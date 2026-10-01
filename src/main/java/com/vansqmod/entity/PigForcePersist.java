package com.vansqmod.entity;

import com.vansqmod.VansqMod;
import com.vansqmod.mixin.PersistentEntitySectionManagerAccessor;
import com.vansqmod.mixin.ServerLevelAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Pigs in this pack pass join events but {@code addFreshEntity} often returns false.
 * Force-persist clears orphaned UUIDs and adds through {@code addNewEntityWithoutEvent}.
 * Disk-loaded pigs are only recovered after a short delay so we do not race chunk load.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class PigForcePersist {

    /**
     * Temporarily disabled (2026-09) while investigating a suspected link to server
     * freezes/"Can't keep up" spirals reported while idling in a swamp. This flag
     * short-circuits every entry point (event handlers and the methods the pig
     * mixins call into) so the whole watch/recovery system is inert without having
     * to touch the mixin registrations themselves. The original missing-pig bug this
     * was written for may no longer reproduce on the current mod set - re-enable by
     * flipping this back to true once that's confirmed one way or the other.
     */
    private static final boolean ENABLED = false;

    private static final int MAX_WATCH_TICKS = 40;
    /** Wait for chunk-load add to finish before treating a pig as missing. */
    private static final int DISK_LOAD_GRACE_TICKS = 5;
    private static final int MAX_RECOVERY_ATTEMPTS = 2;
    private static final ThreadLocal<Integer> RECOVERY_DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final Map<UUID, Long> INTENTIONAL_REMOVALS = new ConcurrentHashMap<>();
    /** Breeding children — keep parent-derived VB variants; never biome-reapply. */
    private static final Map<UUID, Long> BREEDING_CHILDREN = new ConcurrentHashMap<>();
    /** Successfully force-added; watch must not spawn a second pig from a snapshot. */
    private static final Set<UUID> FORCE_ADDED = ConcurrentHashMap.newKeySet();
    /** Prevents concurrent post-join / add-failed recoveries for the same pig. */
    private static final Set<UUID> RECOVERING = ConcurrentHashMap.newKeySet();
    private static final ConcurrentLinkedQueue<Watch> WATCHES = new ConcurrentLinkedQueue<>();

    private PigForcePersist() {
    }

    private static final class Watch {
        private final UUID uuid;
        private final int entityId;
        private final double x;
        private final double y;
        private final double z;
        private final float yRot;
        private final float xRot;
        private final CompoundTag saved;
        private final ServerLevel level;
        private final long watchUntilGameTime;
        private final long recoverAfterGameTime;
        private final boolean fromBreeding;
        private final boolean fromDisk;
        private int attempts;

        private Watch(
                UUID uuid,
                int entityId,
                double x,
                double y,
                double z,
                float yRot,
                float xRot,
                CompoundTag saved,
                ServerLevel level,
                long watchUntilGameTime,
                long recoverAfterGameTime,
                boolean fromBreeding,
                boolean fromDisk,
                int attempts) {
            this.uuid = uuid;
            this.entityId = entityId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yRot = yRot;
            this.xRot = xRot;
            this.saved = saved;
            this.level = level;
            this.watchUntilGameTime = watchUntilGameTime;
            this.recoverAfterGameTime = recoverAfterGameTime;
            this.fromBreeding = fromBreeding;
            this.fromDisk = fromDisk;
            this.attempts = attempts;
        }
    }

    @SubscribeEvent
    public static void onBabySpawn(BabyEntitySpawnEvent event) {
        if (!ENABLED) {
            return;
        }
        if (event.getChild() instanceof Pig pig && !pig.level().isClientSide()) {
            markBreedingChild(pig);
        }
    }

    /** Marks a piglet so force-persist never overwrites its parent-derived variant. */
    public static void markBreedingChild(Pig pig) {
        if (!ENABLED || pig == null || pig.level().isClientSide()) {
            return;
        }
        BREEDING_CHILDREN.put(pig.getUUID(), pig.level().getGameTime() + 200L);
    }

    /**
     * Only KILLED / DISCARDED are intentional. Unload and dimension changes must remain
     * recoverable so pigs persist across leave/rejoin.
     */
    public static void markIntentionalRemoval(Entity entity, Entity.RemovalReason reason) {
        if (!ENABLED || !(entity instanceof Pig) || entity.level().isClientSide()) {
            return;
        }
        if (reason != Entity.RemovalReason.KILLED && reason != Entity.RemovalReason.DISCARDED) {
            FORCE_ADDED.remove(entity.getUUID());
            return;
        }
        INTENTIONAL_REMOVALS.put(entity.getUUID(), entity.level().getGameTime() + 100L);
        FORCE_ADDED.remove(entity.getUUID());
    }

    public static void onAddFinished(ServerLevel level, Pig pig, boolean addReturned) {
        if (!ENABLED || RECOVERY_DEPTH.get() > 0) {
            return;
        }
        if (addReturned && isPresent(level, pig)) {
            return;
        }
        level.getServer().execute(() -> verifyAndRecover(level, pig, addReturned ? "post-add" : "add-failed"));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJoinDone(EntityJoinLevelEvent event) {
        if (!ENABLED || event.getLevel().isClientSide() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!(event.getEntity() instanceof Pig pig) || event.isCanceled() || RECOVERY_DEPTH.get() > 0) {
            return;
        }

        boolean fromDisk = event.loadedFromDisk();
        boolean breeding = pig.isBaby() || isBreedingChild(pig.getUUID(), level.getGameTime());
        if (pig.isBaby()) {
            BREEDING_CHILDREN.putIfAbsent(pig.getUUID(), level.getGameTime() + 200L);
        }

        long now = level.getGameTime();
        WATCHES.add(new Watch(
                pig.getUUID(),
                pig.getId(),
                pig.getX(),
                pig.getY(),
                pig.getZ(),
                pig.getYRot(),
                pig.getXRot(),
                snapshot(pig),
                level,
                now + MAX_WATCH_TICKS,
                now + (fromDisk ? DISK_LOAD_GRACE_TICKS : 1L),
                breeding,
                fromDisk,
                0));

        // Disk loads: do not force-add on the same tick — races chunk registration and
        // was deleting / failing to persist pigs across rejoin. Watch handles recovery.
        if (!fromDisk) {
            level.getServer().execute(() -> verifyAndRecover(level, pig, "post-join"));
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        WATCHES.clear();
        FORCE_ADDED.clear();
        RECOVERING.clear();
        INTENTIONAL_REMOVALS.clear();
        BREEDING_CHILDREN.clear();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ENABLED) {
            return;
        }
        if (WATCHES.isEmpty() && INTENTIONAL_REMOVALS.isEmpty() && BREEDING_CHILDREN.isEmpty()) {
            return;
        }
        long now = event.getServer().overworld().getGameTime();
        INTENTIONAL_REMOVALS.entrySet().removeIf(e -> e.getValue() < now);
        BREEDING_CHILDREN.entrySet().removeIf(e -> e.getValue() < now);

        Iterator<Watch> it = WATCHES.iterator();
        while (it.hasNext()) {
            Watch watch = it.next();
            ServerLevel level = watch.level;
            if (level.getServer() == null || !level.getServer().isRunning()) {
                it.remove();
                continue;
            }

            long gameTime = level.getGameTime();
            if (isIntentionallyRemoved(watch.uuid, gameTime) || FORCE_ADDED.contains(watch.uuid)) {
                it.remove();
                continue;
            }

            boolean tracked = level.getEntity(watch.uuid) != null || level.getEntity(watch.entityId) != null;
            boolean inSection = countPigsInSection(level, watch.x, watch.y, watch.z, watch.uuid) > 0;
            if (tracked || inSection) {
                if (gameTime >= watch.watchUntilGameTime) {
                    it.remove();
                }
                continue;
            }

            // Still inside grace period (especially disk load) — wait.
            if (gameTime < watch.recoverAfterGameTime) {
                continue;
            }

            if (watch.attempts >= MAX_RECOVERY_ATTEMPTS) {
                VansqMod.LOGGER.error(
                        "[PigForcePersist] gave up recovering pig uuid={} at {},{},{} fromDisk={}",
                        watch.uuid,
                        watch.x,
                        watch.y,
                        watch.z,
                        watch.fromDisk);
                it.remove();
                continue;
            }

            watch.attempts++;
            VansqMod.LOGGER.warn(
                    "[PigForcePersist] pig missing uuid={} fromDisk={} breeding={} — recovering (attempt {})",
                    watch.uuid,
                    watch.fromDisk,
                    watch.fromBreeding,
                    watch.attempts);

            // Always restore the same UUID + saved NBT (keeps breed/variant).
            boolean recovered = forceSpawnFromSnapshot(level, watch, watch.fromDisk ? "disk-recover" : "watch");
            if (recovered || watch.attempts >= MAX_RECOVERY_ATTEMPTS || gameTime >= watch.watchUntilGameTime) {
                it.remove();
            }
        }
    }

    private static void verifyAndRecover(ServerLevel level, Pig pig, String reason) {
        if (pig == null || level.getServer() == null) {
            return;
        }
        UUID uuid = pig.getUUID();
        if (isIntentionallyRemoved(uuid, level.getGameTime()) || isPresent(level, pig) || FORCE_ADDED.contains(uuid)) {
            return;
        }
        if (!RECOVERING.add(uuid)) {
            return;
        }
        try {
            VansqMod.LOGGER.warn(
                    "[PigForcePersist] pig not present ({}) uuid={} removed={} isAddedToLevel={} — force-adding",
                    reason,
                    uuid,
                    pig.isRemoved(),
                    pig.isAddedToLevel());
            if (!pig.isRemoved() && forceAddPig(level, pig, reason)) {
                return;
            }
            // Fall back to NBT snapshot with the same UUID (preserves variant).
            forceSpawnFromSnapshot(
                    level,
                    new Watch(
                            uuid,
                            pig.getId(),
                            pig.getX(),
                            pig.getY(),
                            pig.getZ(),
                            pig.getYRot(),
                            pig.getXRot(),
                            snapshot(pig),
                            level,
                            level.getGameTime() + MAX_WATCH_TICKS,
                            level.getGameTime(),
                            pig.isBaby() || isBreedingChild(uuid, level.getGameTime()),
                            reason.contains("disk"),
                            0),
                    reason + "-replacement");
        } finally {
            RECOVERING.remove(uuid);
        }
    }

    private static boolean isPresent(ServerLevel level, Pig pig) {
        if (level.getEntity(pig.getUUID()) != null || level.getEntity(pig.getId()) != null) {
            return true;
        }
        return countPigsInSection(level, pig.getX(), pig.getY(), pig.getZ(), pig.getUUID()) > 0;
    }

    private static int countPigsInSection(ServerLevel level, double x, double y, double z, UUID uuid) {
        AABB box = new AABB(x - 2.0D, y - 2.0D, z - 2.0D, x + 2.0D, y + 2.0D, z + 2.0D);
        return level.getEntities(EntityType.PIG, box, e -> e.getUUID().equals(uuid)).size();
    }

    private static boolean isIntentionallyRemoved(UUID uuid, long gameTime) {
        Long until = INTENTIONAL_REMOVALS.get(uuid);
        return until != null && until >= gameTime;
    }

    private static boolean isBreedingChild(UUID uuid, long gameTime) {
        Long until = BREEDING_CHILDREN.get(uuid);
        return until != null && until >= gameTime;
    }

    /**
     * Biome re-apply is only for fresh {@code addFreshEntity} failures. Loaded pigs,
     * breeding babies, and NBT snapshot copies must keep their existing variant.
     */
    private static boolean shouldAssignBiomeVariant(Pig pig, String reason) {
        if (pig.isBaby() || isBreedingChild(pig.getUUID(), pig.level().getGameTime())) {
            return false;
        }
        if (reason == null) {
            return false;
        }
        if (reason.contains("post-join")
                || reason.contains("disk")
                || reason.contains("watch")
                || reason.contains("replacement")) {
            return false;
        }
        return reason.startsWith("add-failed") || reason.startsWith("post-add");
    }

    /**
     * Bypass EntityJoinLevelEvent and clear any orphaned UUID reservation.
     */
    public static boolean forceAddPig(ServerLevel level, Pig pig, String reason) {
        if (!ENABLED || RECOVERY_DEPTH.get() >= MAX_RECOVERY_ATTEMPTS) {
            return false;
        }
        if (pig.isRemoved()) {
            VansqMod.LOGGER.warn("[PigForcePersist] cannot force-add removed pig ({})", reason);
            return false;
        }
        if (isPresent(level, pig) || FORCE_ADDED.contains(pig.getUUID())) {
            return true;
        }

        RECOVERY_DEPTH.set(RECOVERY_DEPTH.get() + 1);
        try {
            PersistentEntitySectionManager<Entity> manager =
                    ((ServerLevelAccessor) level).vansqmod$getEntityManager();
            PersistentEntitySectionManagerAccessor accessor =
                    (PersistentEntitySectionManagerAccessor) (Object) manager;
            Set<UUID> known = accessor.vansqmod$getKnownUuids();
            boolean wasKnown = known.remove(pig.getUUID());
            boolean added = accessor.vansqmod$addNewEntityWithoutEvent(pig);
            if (added) {
                if (!pig.isAddedToLevel()) {
                    pig.onAddedToLevel();
                }
                // Ensure farm animals are not culled before the next save.
                pig.setPersistenceRequired();
                FORCE_ADDED.add(pig.getUUID());
                boolean assignBiome = shouldAssignBiomeVariant(pig, reason);
                if (assignBiome) {
                    VanillaBackportPigVariants.applyAfterForceAdd(level, pig);
                }
                VansqMod.LOGGER.warn(
                        "[PigForcePersist] force-added pig uuid={} clearedOrphanUuid={} assignBiome={} ({})",
                        pig.getUUID(),
                        wasKnown,
                        assignBiome,
                        reason);
                return true;
            }
            VansqMod.LOGGER.error(
                    "[PigForcePersist] addNewEntityWithoutEvent failed uuid={} wasKnown={} ({})",
                    pig.getUUID(),
                    wasKnown,
                    reason);
            return false;
        } catch (Exception e) {
            VansqMod.LOGGER.error("[PigForcePersist] force-add threw ({})", reason, e);
            return false;
        } finally {
            RECOVERY_DEPTH.set(RECOVERY_DEPTH.get() - 1);
        }
    }

    private static boolean forceSpawnFromSnapshot(ServerLevel level, Watch watch, String reason) {
        if (RECOVERY_DEPTH.get() >= MAX_RECOVERY_ATTEMPTS || FORCE_ADDED.contains(watch.uuid)) {
            return false;
        }
        if (level.getEntity(watch.uuid) != null) {
            return true;
        }
        Pig copy = EntityType.PIG.create(level);
        if (copy == null) {
            return false;
        }
        CompoundTag tag = watch.saved.copy();
        tag.remove("UUID");
        copy.load(tag);
        // Keep the original UUID so rejoin restores the same pig + breed NBT.
        copy.setUUID(watch.uuid);
        copy.moveTo(watch.x, watch.y, watch.z, watch.yRot, watch.xRot);
        copy.setPersistenceRequired();
        if (watch.fromBreeding) {
            BREEDING_CHILDREN.put(copy.getUUID(), level.getGameTime() + 200L);
        }
        return forceAddPig(level, copy, reason);
    }

    private static CompoundTag snapshot(Pig pig) {
        CompoundTag tag = new CompoundTag();
        pig.saveWithoutId(tag);
        tag.remove("UUID");
        return tag;
    }
}
