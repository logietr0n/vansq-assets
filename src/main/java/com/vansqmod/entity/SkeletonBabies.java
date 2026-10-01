package com.vansqmod.entity;

import com.vansqmod.VansqMod;
import com.vansqmod.debug.VansqDebugState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Baby skeletons share their adult entity type. Vanilla {@code skeleton} babies are
 * the Born in Chaos Baby Skeleton; {@code wither_skeleton} babies are the Bone Imp.
 * Controlled baby skeletons and baby skeleton minions stay as their own types.
 */
public final class SkeletonBabies {

    public static final float WIDTH = 0.6F;
    public static final float HEIGHT = 1.3F;
    public static final float EYE_HEIGHT = 1.1F;
    public static final float NATURAL_CHANCE = 0.10F;
    public static final float ALT_TEXTURE_CHANCE = 0.4F;

    public static final String TEXTURE_DEFAULT = "baby_skeleton";
    public static final String TEXTURE_ALT = "baby_skeleton_alternative";
    public static final String TEXTURE_IMP = "bone_imp";

    public static final ResourceLocation BIC_BABY_SKELETON =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "baby_skeleton");
    public static final ResourceLocation BIC_BONE_IMP =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "bone_imp");

    private static final ResourceLocation BIC_ENTITIES =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "textures/entities/");
    private static final ResourceLocation GEO_BABY_SKELETON =
            ResourceLocation.parse("born_in_chaos_v1:geo/babyskeleton2.geo.json");
    private static final ResourceLocation GEO_BONE_IMP =
            ResourceLocation.parse("born_in_chaos_v1:geo/boneimp2.geo.json");
    private static final ResourceLocation ANIM_BABY_SKELETON =
            ResourceLocation.parse("born_in_chaos_v1:animations/babyskeleton2.animation.json");
    private static final ResourceLocation ANIM_BONE_IMP =
            ResourceLocation.parse("born_in_chaos_v1:animations/boneimp2.animation.json");
    private static final ResourceLocation ANIM_VARIANT =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "animations/entity/skeleton_baby.animation.json");

    private static final ThreadLocal<Boolean> PAIR_SPAWN = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final ThreadLocal<Boolean> FORCE_BABY = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final String BABY_ROLL_KEY = "vansqBabyRolled";
    private static final String BABY_DECISION_KEY = "vansqBabyDecision";
    private static final String PENDING_BABY_KEY = "vansqPendingBaby";
    private static final double MIN_PAIR_SPREAD = 0.55D;
    private static final double MAX_PAIR_SPREAD = 1.4D;

    private SkeletonBabies() {
    }

    public static boolean isFamily(LivingEntity entity) {
        return entity instanceof AbstractSkeleton || entity instanceof Mellowed;
    }

    /**
     * Synched baby flag. Spawn eggs and rendering read this directly because
     * {@link LivingEntity#isBaby()} stays false for skeletons unless our override is the method that runs.
     */
    public static boolean isMarkedBaby(LivingEntity entity) {
        if (entity instanceof AbstractSkeleton) {
            return entity.getEntityData().get(SkeletonBabyData.BABY);
        }
        return entity.isBaby();
    }

    public static boolean isWitherBaby(LivingEntity entity) {
        return isMarkedBaby(entity) && entity instanceof WitherSkeleton;
    }

    public static boolean usesGeo(LivingEntity entity) {
        return isMarkedBaby(entity) && isFamily(entity);
    }

    public static boolean usesBoneImpGeo(LivingEntity entity) {
        return isWitherBaby(entity);
    }

    public static boolean rollsNaturalBaby(LivingEntity entity) {
        return entity instanceof AbstractSkeleton || entity instanceof Mellowed;
    }

    public static boolean isWorldSpawn(MobSpawnType spawnType) {
        return spawnType != MobSpawnType.COMMAND
                && spawnType != MobSpawnType.BUCKET
                && spawnType != MobSpawnType.DISPENSER
                && spawnType != MobSpawnType.CONVERSION
                && spawnType != MobSpawnType.BREEDING;
    }

    public static boolean isPairSpawn() {
        return PAIR_SPAWN.get();
    }

    /**
     * Hitbox updates and extra pair spawns load chunks. Doing that from Distant
     * Horizons / structure worldgen waits forever on the generating chunk.
     */
    public static boolean canTouchChunks(Entity entity) {
        if (!(entity.level() instanceof ServerLevel server)) {
            return !entity.level().isClientSide();
        }
        return server.getServer().isSameThread() && server.hasChunkAt(entity.blockPosition());
    }

    /**
     * 10% of skeleton/mellowed spawn attempts become a baby pair, including
     * spawn eggs. Commands, conversions, and breeding are excluded. Mellowed
     * babies keep Mellowed AI; other babies use Born in Chaos melee rules.
     */
    public static void runAsForcedBaby(Runnable action) {
        FORCE_BABY.set(true);
        try {
            action.run();
        } finally {
            FORCE_BABY.set(false);
        }
    }

    /**
     * Writes the synched baby flag and calls {@link Mob#setBaby}. Natural
     * skeleton rolls cannot rely on {@code setBaby} alone: LivingEntity's method
     * is empty, and AbstractSkeleton does not declare an override.
     */
    public static void markAsBaby(Mob mob) {
        if (mob instanceof AbstractSkeleton) {
            mob.getEntityData().set(SkeletonBabyData.BABY, true);
        }
        mob.setBaby(true);
        if (mob instanceof AbstractSkeleton && !isMarkedBaby(mob)) {
            mob.getEntityData().set(SkeletonBabyData.BABY, true);
        }
    }

    public static void tryReplaceWithBabyPair(Mob mob, MobSpawnType spawnType) {
        if (FORCE_BABY.get()) {
            convertToBaby(mob, false);
            return;
        }
        if (mob.level().isClientSide() || PAIR_SPAWN.get() || HostileBabyGroups.isSpawningExtra()) {
            if (isMarkedBaby(mob)) {
                prepareBaby(mob);
            }
            return;
        }
        if (isMarkedBaby(mob)) {
            markBabyRollDone(mob);
            prepareBaby(mob);
            if (hasPendingBaby(mob)) {
                applyPendingBaby(mob);
            }
            return;
        }
        if (!isWorldSpawn(spawnType) || !rollsNaturalBaby(mob)) {
            markBabyRollDone(mob);
            return;
        }
        if (applyPendingBaby(mob)) {
            return;
        }
        // Worldgen / DH threads cannot shrink the model or spawn a partner.
        // Remember the 10% / chaos decision, but do not mark the roll finished
        // until the entity is on the server thread with a loaded chunk.
        if (!canTouchChunks(mob)) {
            rememberBabyDecision(mob);
            return;
        }
        if (isBabyRollDone(mob)) {
            return;
        }
        boolean baby = consumeBabyDecisionOrRoll(mob);
        markBabyRollDone(mob);
        if (baby) {
            convertToBaby(mob, true);
            return;
        }
        ensureAdultBow(mob);
    }

    private static void convertToBaby(Mob mob, boolean spawnPartner) {
        markAsBaby(mob);
        markBabyRollDone(mob);
        prepareBaby(mob);
        if (spawnPartner) {
            spawnPartner(mob);
        }
    }

    private static boolean hasBabyDecision(Mob mob) {
        return mob.getPersistentData().contains(BABY_DECISION_KEY);
    }

    private static void rememberBabyDecision(Mob mob) {
        if (hasBabyDecision(mob) || isBabyRollDone(mob)) {
            return;
        }
        boolean baby = VansqDebugState.rareEventSucceeds(mob.getRandom(), NATURAL_CHANCE);
        mob.getPersistentData().putBoolean(BABY_DECISION_KEY, baby);
        if (baby) {
            mob.getPersistentData().putBoolean(PENDING_BABY_KEY, true);
        }
    }

    private static boolean consumeBabyDecisionOrRoll(Mob mob) {
        if (hasBabyDecision(mob)) {
            boolean baby = mob.getPersistentData().getBoolean(BABY_DECISION_KEY);
            mob.getPersistentData().remove(BABY_DECISION_KEY);
            return baby;
        }
        return VansqDebugState.rareEventSucceeds(mob.getRandom(), NATURAL_CHANCE);
    }

    /**
     * Finish a baby conversion that was rolled during worldgen, once the entity
     * is on the server thread with a loaded chunk.
     */
    public static boolean hasPendingBaby(Mob mob) {
        return mob.getPersistentData().getBoolean(PENDING_BABY_KEY);
    }

    public static boolean applyPendingBaby(Mob mob) {
        if (!hasPendingBaby(mob)) {
            return false;
        }
        if (!canTouchChunks(mob)) {
            markAsBaby(mob);
            return true;
        }
        mob.getPersistentData().remove(PENDING_BABY_KEY);
        mob.getPersistentData().remove(BABY_DECISION_KEY);
        convertToBaby(mob, true);
        return true;
    }

    public static boolean isBabyRollDone(Mob mob) {
        return isMarkedBaby(mob) || mob.getPersistentData().getBoolean(BABY_ROLL_KEY);
    }

    public static void markBabyRollDone(Mob mob) {
        mob.getPersistentData().putBoolean(BABY_ROLL_KEY, true);
    }

    /**
     * Adult skeletons always spawn with a bow. Used when a deferred roll decides
     * they stay adult, or when a previous worldgen pass emptied the hand.
     */
    public static void ensureAdultBow(Mob mob) {
        if (isMarkedBaby(mob) || mob instanceof WitherSkeleton || mob instanceof Mellowed) {
            return;
        }
        if (!(mob instanceof AbstractSkeleton skeleton) || !mob.getMainHandItem().isEmpty()) {
            return;
        }
        mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        skeleton.reassessWeaponGoal();
    }

    public static void spawnNearbyBaby(Mob source) {
        spawnPartner(source);
    }

    private static void spawnPartner(Mob source) {
        if (!(source.level() instanceof ServerLevel level) || !canTouchChunks(source)) {
            return;
        }
        Vec3 center = source.position();
        double angle = source.getRandom().nextDouble() * (Math.PI * 2.0D);
        double opposite = angle + Math.PI + (source.getRandom().nextDouble() - 0.5D) * 0.7D;
        Vec3 first = standNear(level, source, center, angle, pairSpread(source));
        Vec3 second = standNear(level, source, center, opposite, pairSpread(source));
        if (second.distanceToSqr(first) < 0.25D) {
            second = standNear(level, source, first, opposite, MIN_PAIR_SPREAD + 0.35D);
        }
        source.moveTo(first.x, first.y, first.z, source.getYRot(), source.getXRot());
        PAIR_SPAWN.set(true);
        try {
            Entity created = source.getType().create(level);
            if (!(created instanceof Mob extra)) {
                if (created != null) {
                    created.discard();
                }
                return;
            }
            extra.moveTo(
                    second.x,
                    second.y,
                    second.z,
                    source.getYRot() + (source.getRandom().nextFloat() - 0.5F) * 40.0F,
                    0.0F);
            markAsBaby(extra);
            markBabyRollDone(extra);
            extra.finalizeSpawn(
                    level,
                    level.getCurrentDifficultyAt(extra.blockPosition()),
                    MobSpawnType.REINFORCEMENT,
                    null);
            if (source.isPersistenceRequired()) {
                extra.setPersistenceRequired();
            }
            if (!level.addFreshEntity(extra)) {
                extra.discard();
            }
        } finally {
            PAIR_SPAWN.set(false);
        }
    }

    static Vec3 placeNear(ServerLevel level, Mob prototype, Vec3 from) {
        double angle = prototype.getRandom().nextDouble() * (Math.PI * 2.0D);
        return standNear(level, prototype, from, angle, pairSpread(prototype));
    }

    private static double pairSpread(Mob mob) {
        return MIN_PAIR_SPREAD + mob.getRandom().nextDouble() * (MAX_PAIR_SPREAD - MIN_PAIR_SPREAD);
    }

    private static Vec3 standNear(ServerLevel level, Mob prototype, Vec3 from, double angle, double dist) {
        double x = from.x + Math.cos(angle) * dist;
        double z = from.z + Math.sin(angle) * dist;
        BlockPos column = BlockPos.containing(x, from.y, z);
        if (!level.hasChunkAt(column)) {
            return from;
        }
        AABB box = prototype.getDimensions(prototype.getPose()).makeBoundingBox(x, from.y, z);
        for (int dy = 2; dy >= -3; dy--) {
            AABB shifted = box.move(0.0D, dy, 0.0D);
            if (!level.noCollision(shifted)) {
                continue;
            }
            BlockPos floor = BlockPos.containing(x, from.y + dy - 0.05D, z);
            if (!level.getBlockState(floor).isSolid()) {
                continue;
            }
            return new Vec3(x, from.y + dy, z);
        }
        return new Vec3(x, from.y, z);
    }

    public static boolean isMergeSource(EntityType<?> type) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return BIC_BABY_SKELETON.equals(id) || BIC_BONE_IMP.equals(id);
    }

    public static boolean isMergeSource(Entity entity) {
        return entity != null && isMergeSource(entity.getType());
    }

    public static EntityType<?> replacementType(EntityType<?> source) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(source);
        if (BIC_BONE_IMP.equals(id)) {
            return EntityType.WITHER_SKELETON;
        }
        return EntityType.SKELETON;
    }

    public static String textureKey(LivingEntity entity) {
        if (entity instanceof SkeletonBabyAccess access) {
            String key = access.vansqmod$getSkeletonBabyTexture();
            if (key != null && !key.isEmpty()) {
                return key;
            }
        }
        if (usesBoneImpGeo(entity)) {
            return TEXTURE_IMP;
        }
        return TEXTURE_DEFAULT;
    }

    public static void setTextureKey(LivingEntity entity, String texture) {
        if (entity instanceof SkeletonBabyAccess access) {
            access.vansqmod$setSkeletonBabyTexture(texture);
        }
    }

    public static void rollTexture(LivingEntity entity) {
        if (usesBoneImpGeo(entity)) {
            setTextureKey(entity, TEXTURE_IMP);
            return;
        }
        String current = null;
        if (entity instanceof SkeletonBabyAccess access) {
            current = access.vansqmod$getSkeletonBabyTexture();
        }
        if (current != null && !current.isEmpty()) {
            return;
        }
        setTextureKey(
                entity,
                entity.getRandom().nextDouble() < ALT_TEXTURE_CHANCE ? TEXTURE_ALT : TEXTURE_DEFAULT);
    }

    public static void prepareBaby(Mob mob) {
        if (!isMarkedBaby(mob) || !canTouchChunks(mob)) {
            return;
        }
        rollTexture(mob);
        mob.setCanPickUpLoot(false);
        if (!(mob instanceof Mellowed)) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                mob.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
        mob.refreshDimensions();
        SkeletonBabyAttributes.apply(mob);
        if (mob instanceof AbstractSkeleton skeleton) {
            skeleton.reassessWeaponGoal();
            SkeletonBabyAi.apply(skeleton);
        }
    }

    public static ResourceLocation bodyTexture(LivingEntity entity) {
        String key = textureKey(entity);
        if (usesBoneImpGeo(entity) || entity.getType() == EntityType.SKELETON) {
            return BIC_ENTITIES.withPath(BIC_ENTITIES.getPath() + key + ".png");
        }
        String variant = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        String file = TEXTURE_ALT.equals(key) ? variant + "_baby_alternative.png" : variant + "_baby.png";
        return ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "textures/entity/" + file);
    }

    public static ResourceLocation geoModel(LivingEntity entity) {
        if (usesBoneImpGeo(entity)) {
            return GEO_BONE_IMP;
        }
        if (entity.getType() == EntityType.SKELETON) {
            return GEO_BABY_SKELETON;
        }
        String variant = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        return ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "geo/entity/" + variant + "_baby.geo.json");
    }

    public static ResourceLocation geoAnimation(LivingEntity entity) {
        if (entity instanceof Mellowed) {
            return ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "animations/entity/mellowed.animation.json");
        }
        if (usesBoneImpGeo(entity)) {
            return ANIM_BONE_IMP;
        }
        if (entity.getType() == EntityType.SKELETON) {
            return ANIM_BABY_SKELETON;
        }
        return ANIM_VARIANT;
    }

    public static String idleAnimation(LivingEntity entity) {
        return usesBoneImpGeo(entity) ? "animation.idle.new" : "idle";
    }

    public static ResourceLocation enchantEyes(LivingEntity entity) {
        if (usesBoneImpGeo(entity)) {
            return ResourceLocation.fromNamespaceAndPath(
                    VansqMod.MODID, "textures/entity/enchant_eye/enchanted_bone_imp_eyes.png");
        }
        String file = TEXTURE_ALT.equals(textureKey(entity))
                ? "enchanted_baby_skeleton_alternative_eyes.png"
                : "enchanted_baby_skeleton_eyes.png";
        return ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "textures/entity/enchant_eye/" + file);
    }

    public static @Nullable String bicTextureName(Entity entity) {
        try {
            Object value = entity.getClass().getMethod("getTexture").invoke(entity);
            return value instanceof String name ? name : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
