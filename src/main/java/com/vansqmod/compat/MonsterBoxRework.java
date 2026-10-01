package com.vansqmod.compat;

import com.vansqmod.entity.SkeletonBabies;
import com.vansqmod.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.EventHooks;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 * Quark monster box: short scripted burst, then a 15-minute cooldown before it can arm again.
 */
public final class MonsterBoxRework {

    public static final String NBT_STAGE = "vansqmod_box_stage";
    public static final String NBT_TICKS = "vansqmod_box_ticks";
    public static final String NBT_SWARM = "vansqmod_box_swarm";
    public static final String TAG_SPAWNED = "quark:monster_box_spawned";

    public static final int STAGE_IDLE = 0;
    public static final int STAGE_WINDUP = 1;
    public static final int STAGE_SWARM = 2;
    public static final int STAGE_WAIT = 3;
    public static final int STAGE_FINALE = 4;
    public static final int STAGE_SPENT = 5;

    private static final int WINDUP_TICKS = 40;
    private static final int SWARM_INTERVAL = 20;
    private static final int SWARM_GROUPS = 3;
    private static final int WAIT_TICKS = 60;
    private static final int HARD_EXTRA_TICKS = 20;
    private static final int REARM_TICKS = 15 * 60 * 20;
    private static final double ACTIVATION_RANGE = 2.5D;
    private static final int SPAWN_RANGE = 4;
    private static final int SPAWN_ATTEMPTS = 16;
    private static final ResourceLocation MONSTER_BOX_ID =
            ResourceLocation.fromNamespaceAndPath("quark", "monster_box");
    private static final ResourceLocation GROWL =
            ResourceLocation.fromNamespaceAndPath("quark", "block.monster_box.growl");
    private static final ResourceLocation BABY_SPIDER =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "baby_spider");
    private static final ResourceLocation PEEPER =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "peeper");
    private static final ResourceLocation BONESCALLER =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "bonescaller");
    private static final ResourceLocation SPIRIT_OF_CHAOS =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "spiritof_chaos");
    private static final ResourceLocation RESTLESS_SPIRIT =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "restless_spirit");
    private static final ResourceLocation CORPSE_FLY =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "corpse_fly");
    private static final ResourceLocation SWARMER =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "swarmer");
    private static final ResourceLocation SIAMESE_SKELETONS =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "siamese_skeletons");
    private static final ResourceLocation JUGGERNAUT =
            ResourceLocation.fromNamespaceAndPath("hominid", "juggernaut");
    private static final ResourceLocation CENTIPEDE =
            ResourceLocation.fromNamespaceAndPath("alexsmobs", "centipede_head");
    private static final ResourceLocation SHADE =
            ResourceLocation.fromNamespaceAndPath("peaceless", "shade");
    private static final ResourceLocation SPAWN_MOB_SOUND =
            ResourceLocation.fromNamespaceAndPath("subtle_effects", "block.monster_spawner.spawn_mob");
    private static final ResourceLocation AMBIENT_SOUND =
            ResourceLocation.fromNamespaceAndPath("subtle_effects", "block.monster_spawner.ambient");

    private static final Map<BlockEntity, Data> DATA = Collections.synchronizedMap(new WeakHashMap<>());
    private static final ThreadLocal<Boolean> BYPASS_INCONTROL = ThreadLocal.withInitial(() -> false);

    private MonsterBoxRework() {
    }

    public static boolean isMonsterBox(BlockState state) {
        return state != null && isMonsterBox(state.getBlock());
    }

    public static boolean isMonsterBox(Block block) {
        return block != null && MONSTER_BOX_ID.equals(BuiltInRegistries.BLOCK.getKey(block));
    }

    public static boolean isMonsterBoxEntity(BlockEntity entity) {
        return entity != null && isMonsterBox(entity.getBlockState());
    }

    public static int stageOf(BlockEntity box) {
        if (box == null) {
            return STAGE_IDLE;
        }
        Data data = DATA.get(box);
        return data == null ? STAGE_IDLE : data.stage;
    }

    public static boolean isBypassingInControl() {
        return Boolean.TRUE.equals(BYPASS_INCONTROL.get());
    }

    public static Object ignoreInControlDeny(Object result) {
        if (!isBypassingInControl() || !(result instanceof Enum<?> value)) {
            return result;
        }
        String name = value.name();
        if (!"DENY".equals(name) && !"DENY_WITH_ACTIONS".equals(name)) {
            return result;
        }
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Enum<?> allowed = Enum.valueOf((Class) value.getClass(), "DEFAULT");
        return allowed;
    }

    public static void forEachTracked(Consumer<BlockEntity> consumer) {
        synchronized (DATA) {
            for (BlockEntity box : DATA.keySet()) {
                if (box != null) {
                    consumer.accept(box);
                }
            }
        }
    }

    private static void bypassInControl(Runnable action) {
        BYPASS_INCONTROL.set(true);
        try {
            action.run();
        } finally {
            BYPASS_INCONTROL.remove();
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity box) {
        Data data = DATA.computeIfAbsent(box, unused -> new Data());
        if (data.stage == STAGE_SPENT) {
            if (level instanceof ServerLevel && !level.isClientSide()) {
                data.ticks++;
                if (data.ticks >= REARM_TICKS) {
                    data.stage = STAGE_IDLE;
                    data.ticks = 0;
                    data.swarmSpawned = 0;
                    sync(box);
                }
            }
            return;
        }
        emitPhaseParticles(level, pos, data.stage);
        emitAmbientSound(level, pos, data.stage);

        if (level.getDifficulty() == Difficulty.PEACEFUL && data.stage == STAGE_IDLE) {
            return;
        }

        if (data.stage == STAGE_IDLE) {
            if (!playerInRange(level, pos)) {
                return;
            }
            data.stage = STAGE_WINDUP;
            data.ticks = 0;
            if (!level.isClientSide()) {
                playGrowl(level, pos);
                sync(box);
            }
        }

        if (level.isClientSide()) {
            if (data.stage == STAGE_WINDUP) {
                data.ticks++;
                if (data.ticks > WINDUP_TICKS) {
                    data.stage = STAGE_SWARM;
                    data.ticks = 0;
                }
            }
            return;
        }
        if (!(level instanceof ServerLevel server)) {
            return;
        }

        switch (data.stage) {
            case STAGE_WINDUP -> {
                data.ticks++;
                if (data.ticks >= WINDUP_TICKS) {
                    data.stage = STAGE_SWARM;
                    data.ticks = 0;
                    data.swarmSpawned = 0;
                    spawnSwarmGroup(server, pos);
                    data.swarmSpawned = 1;
                    sync(box);
                }
            }
            case STAGE_SWARM -> {
                data.ticks++;
                if (data.ticks >= SWARM_INTERVAL) {
                    data.ticks = 0;
                    spawnSwarmGroup(server, pos);
                    data.swarmSpawned++;
                    if (data.swarmSpawned >= SWARM_GROUPS) {
                        data.stage = STAGE_WAIT;
                    }
                    sync(box);
                }
            }
            case STAGE_WAIT -> {
                data.ticks++;
                if (data.ticks >= WAIT_TICKS) {
                    data.ticks = 0;
                    spawnFinaleGroup(server, pos);
                    if (server.getDifficulty() == Difficulty.HARD) {
                        data.stage = STAGE_FINALE;
                        sync(box);
                    } else {
                        extinguish(server, pos, box, data);
                    }
                }
            }
            case STAGE_FINALE -> {
                data.ticks++;
                if (data.ticks >= HARD_EXTRA_TICKS) {
                    spawnFinaleGroup(server, pos);
                    extinguish(server, pos, box, data);
                }
            }
            default -> {
            }
        }
    }

    public static void save(BlockEntity box, CompoundTag tag) {
        Data data = DATA.get(box);
        if (data == null) {
            return;
        }
        tag.putInt(NBT_STAGE, data.stage);
        tag.putInt(NBT_TICKS, data.ticks);
        tag.putInt(NBT_SWARM, data.swarmSpawned);
    }

    public static void load(BlockEntity box, CompoundTag tag) {
        if (!tag.contains(NBT_STAGE)) {
            return;
        }
        Data data = DATA.computeIfAbsent(box, unused -> new Data());
        data.stage = tag.getInt(NBT_STAGE);
        data.ticks = tag.getInt(NBT_TICKS);
        data.swarmSpawned = tag.getInt(NBT_SWARM);
    }

    private static void extinguish(ServerLevel level, BlockPos pos, BlockEntity box, Data data) {
        data.stage = STAGE_SPENT;
        data.ticks = 0;
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 0.8F + level.random.nextFloat() * 0.2F);
        for (int i = 0; i < 12; i++) {
            double x = pos.getX() + 0.5D + (level.random.nextDouble() - 0.5D);
            double y = pos.getY() + 0.5D + (level.random.nextDouble() - 0.5D);
            double z = pos.getZ() + 0.5D + (level.random.nextDouble() - 0.5D);
            level.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, 0.0D, 0.05D, 0.0D, 0.0D);
        }
        sync(box);
    }

    private static void sync(BlockEntity box) {
        box.setChanged();
        Level level = box.getLevel();
        if (level != null) {
            BlockState state = box.getBlockState();
            level.sendBlockUpdated(box.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
        }
    }

    private static void emitPhaseParticles(Level level, BlockPos pos, int stage) {
        if (!level.isClientSide()) {
            return;
        }
        RandomSource random = level.random;
        int count = switch (stage) {
            case STAGE_IDLE -> random.nextFloat() < 0.05F ? 1 : 0;
            case STAGE_WINDUP -> 5;
            case STAGE_SWARM, STAGE_WAIT, STAGE_FINALE -> 1;
            default -> 0;
        };
        if (count <= 0) {
            return;
        }
        ParticleOptions flame = flameParticle();
        for (int i = 0; i < count; i++) {
            level.addParticle(
                    flame,
                    pos.getX() + random.nextDouble(),
                    pos.getY() + random.nextDouble(),
                    pos.getZ() + random.nextDouble(),
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    private static ParticleOptions flameParticle() {
        if (!ModList.get().isLoaded("dungeonsdelight")) {
            return ParticleTypes.FLAME;
        }
        try {
            Class<?> config = Class.forName("net.yirmiri.dungeonsdelight.DDConfigClient");
            Object spec = config.getField("SPAWNERS_EMIT_GREEN_FLAMES").get(null);
            Object enabled = spec.getClass().getMethod("get").invoke(spec);
            if (!Boolean.TRUE.equals(enabled)) {
                return ParticleTypes.FLAME;
            }
            Class<?> particles = Class.forName("net.yirmiri.dungeonsdelight.core.registry.DDParticles");
            Object supplier = particles.getField("LIVING_FLAME").get(null);
            Object particle = supplier.getClass().getMethod("get").invoke(supplier);
            if (particle instanceof ParticleOptions options) {
                return options;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return ParticleTypes.FLAME;
    }

    private static boolean playerInRange(Level level, BlockPos pos) {
        double rangeSq = ACTIVATION_RANGE * ACTIVATION_RANGE;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        for (Player player : level.players()) {
            if (!player.isSpectator() && player.distanceToSqr(x, y, z) < rangeSq) {
                return true;
            }
        }
        return false;
    }

    private static void playGrowl(Level level, BlockPos pos) {
        BuiltInRegistries.SOUND_EVENT.getOptional(GROWL).ifPresent(sound ->
                level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.5F, 1.0F));
    }

    private static void emitAmbientSound(Level level, BlockPos pos, int stage) {
        if (!level.isClientSide()
                || stage != STAGE_SWARM && stage != STAGE_WAIT && stage != STAGE_FINALE) {
            return;
        }
        RandomSource random = level.random;
        if (random.nextFloat() > 0.02F) {
            return;
        }
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getOptional(AMBIENT_SOUND)
                .orElse(SoundEvents.TRIAL_SPAWNER_AMBIENT);
        float volume = 0.75F + random.nextFloat() * 0.25F;
        float pitch = 0.5F + random.nextFloat();
        level.playLocalSound(pos, sound, SoundSource.BLOCKS, volume, pitch, false);
    }

    private static void playSpawnMobSound(ServerLevel level, BlockPos pos) {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getOptional(SPAWN_MOB_SOUND)
                .orElse(SoundEvents.TRIAL_SPAWNER_SPAWN_MOB);
        RandomSource random = level.random;
        level.playSound(
                null,
                pos,
                sound,
                SoundSource.BLOCKS,
                1.0F,
                1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F
        );
    }

    private static void spawnSwarmGroup(ServerLevel level, BlockPos pos) {
        RandomSource random = level.random;
        switch (random.nextInt(6)) {
            case 0 -> spawnCount(level, pos, zombieType(pos), 2 + random.nextInt(2), false);
            case 1 -> spawnCount(level, pos, EntityType.SKELETON, 1 + random.nextInt(2), false);
            case 2 -> spawnCount(level, pos, EntityType.SPIDER, 2, false);
            case 3 -> spawnCount(level, pos, BABY_SPIDER, 3 + random.nextInt(3), false);
            case 4 -> spawnCount(level, pos, RESTLESS_SPIRIT, 2, false);
            default -> spawnCount(level, pos, CORPSE_FLY, 2, false);
        }
    }

    private static void spawnFinaleGroup(ServerLevel level, BlockPos pos) {
        RandomSource random = level.random;
        if (random.nextBoolean()) {
            switch (random.nextInt(4)) {
                case 0 -> spawnCount(level, pos, PEEPER, 1 + random.nextInt(2), false);
                case 1 -> spawnCount(level, pos, BONESCALLER, 1 + random.nextInt(2), false);
                case 2 -> spawnCount(level, pos, EntityType.SKELETON, 3 + random.nextInt(3), false);
                default -> spawnCount(level, pos, SPIRIT_OF_CHAOS, 1, false);
            }
            return;
        }
        switch (random.nextInt(6)) {
            case 0 -> spawnCount(level, pos, JUGGERNAUT, 1, false);
            case 1 -> spawnCount(level, pos, CENTIPEDE, 1, false);
            case 2 -> spawnCount(level, pos, BABY_SPIDER, 8 + random.nextInt(5), false);
            case 3 -> spawnCount(level, pos, SHADE, 2 + random.nextInt(2), false);
            case 4 -> spawnCount(level, pos, SWARMER, 1 + random.nextInt(2), false);
            default -> spawnCount(level, pos, SIAMESE_SKELETONS, 2 + random.nextInt(2), false);
        }
    }

    private static EntityType<?> zombieType(BlockPos pos) {
        if (pos.getY() < 0) {
            return ModEntityTypes.BOULDERING_ZOMBIE.get();
        }
        return EntityType.ZOMBIE;
    }

    private static void spawnCount(ServerLevel level, BlockPos pos, ResourceLocation id, int count, boolean baby) {
        BuiltInRegistries.ENTITY_TYPE.getOptional(id).ifPresent(type -> spawnCount(level, pos, type, count, baby));
    }

    private static void spawnCount(ServerLevel level, BlockPos pos, EntityType<?> type, int count, boolean baby) {
        if (type == null) {
            return;
        }
        Runnable spawn = () -> bypassInControl(() -> {
            for (int i = 0; i < count; i++) {
                spawnOne(level, pos, type, baby);
            }
        });
        if (baby && (type == EntityType.SKELETON || type == EntityType.STRAY || type == EntityType.BOGGED)) {
            SkeletonBabies.runAsForcedBaby(spawn);
            return;
        }
        spawn.run();
    }

    private static void spawnOne(ServerLevel level, BlockPos pos, EntityType<?> type, boolean baby) {
        RandomSource random = level.random;
        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            double x = pos.getX() + (random.nextDouble() - random.nextDouble()) * SPAWN_RANGE + 0.5D;
            double y = pos.getY() + random.nextInt(3) - 1;
            double z = pos.getZ() + (random.nextDouble() - random.nextDouble()) * SPAWN_RANGE + 0.5D;
            if (!level.noCollision(type.getSpawnAABB(x, y, z))) {
                continue;
            }
            if (tryPlace(level, type, baby, x, y, z)) {
                return;
            }
        }
        tryPlace(level, type, baby, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
    }

    private static boolean tryPlace(
            ServerLevel level,
            EntityType<?> type,
            boolean baby,
            double x,
            double y,
            double z
    ) {
        Entity spawned = type.create(level);
        if (spawned == null) {
            return false;
        }
        spawned.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
        if (spawned instanceof Mob mob) {
            if (!EventHooks.checkSpawnPosition(mob, level, MobSpawnType.SPAWNER)
                    || !mob.checkSpawnObstruction(level)) {
                spawned.discard();
                return false;
            }
            if (baby && !mob.isBaby()) {
                mob.setBaby(true);
            }
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.SPAWNER, null);
        }
        spawned.getPersistentData().putBoolean(TAG_SPAWNED, true);
        if (!level.tryAddFreshEntityWithPassengers(spawned)) {
            spawned.discard();
            return false;
        }
        BlockPos spawnPos = BlockPos.containing(x, y, z);
        level.levelEvent(2004, spawnPos, 0);
        playSpawnMobSound(level, spawnPos);
        level.gameEvent(spawned, GameEvent.ENTITY_PLACE, spawnPos);
        if (spawned instanceof Mob mob) {
            mob.spawnAnim();
        }
        return true;
    }

    private static final class Data {
        private int stage = STAGE_IDLE;
        private int ticks;
        private int swarmSpawned;
    }
}
