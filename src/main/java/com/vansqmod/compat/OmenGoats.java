package com.vansqmod.compat;

import com.vansqmod.debug.VansqDebugState;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.fml.ModList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Replaces Goat Man's omen / followed events with a Sleep Tight-style wake
 * herd. Chance scales with Sleep Quality (1% at 0, 0.2% at 10). Chaos forces
 * the wake roll. One goat in the pack is a Follow Goat that stands still
 * staring until a nearby goat is killed or a 5-second stare, then silently
 * follows and rushes like Goat Man's Followed event.
 */
public final class OmenGoats {

    public static final String TAG_OMEN = "vansq_omen_goat";
    public static final String TAG_FOLLOW = "vansq_follow_goat";
    public static final String TAG_ARMED = "vansq_follow_armed";
    public static final String TAG_STARE = "vansq_follow_stare";
    public static final String TAG_PHASE = "vansq_follow_phase";
    public static final String TAG_PHASE_TICKS = "vansq_follow_phase_ticks";
    public static final String TAG_TARGET = "vansq_follow_target";

    public static final float WAKE_CHANCE_AT_ZERO = 0.01F;
    public static final float WAKE_CHANCE_AT_TEN = 0.002F;
    public static final int QUALITY_MAX = 10;
    public static final int MIN_COUNT = 10;
    public static final int MAX_COUNT = 16;
    public static final int AREA_HALF = 16;
    public static final double MIN_DISTANCE = 8.0D;
    public static final int STARE_TICKS = 20 * 5;
    public static final double KILL_RANGE = 24.0D;
    public static final double STARE_DOT = 0.985D;
    public static final double FOLLOW_LOOK_RANGE = 128.0D;

    private static final int PHASE_FOLLOW = 1;
    private static final int PHASE_RUSH = 2;
    private static final int RUSH_TICKS = 20 * 4;
    private static final double HIT_DISTANCE = 1.4D;
    private static final ResourceLocation GOAT_MAN_ID =
            ResourceLocation.fromNamespaceAndPath("goat_man", "goat_man");
    private static final ResourceLocation[] RUSH_SCREAMS = {
            ResourceLocation.fromNamespaceAndPath("goat_man", "goatscream_1"),
            ResourceLocation.fromNamespaceAndPath("goat_man", "goatscream_2"),
            ResourceLocation.fromNamespaceAndPath("goat_man", "goatscream_7")
    };
    private static final String CONFIG_CLASS = "de.cadentem.goat_man.config.ServerConfig";
    private static final String ENTITY_CLASS = "de.cadentem.goat_man.entities.GoatManEntity";
    private static final String ROLL_CLASS = "de.cadentem.goat_man.entities.goals.Roll";

    private OmenGoats() {
    }

    public static boolean isOmen(Goat goat) {
        return goat.getPersistentData().getBoolean(TAG_OMEN);
    }

    public static boolean isFollow(Goat goat) {
        return goat.getPersistentData().getBoolean(TAG_FOLLOW);
    }

    public static boolean trySpawnForWake(ServerPlayer player) {
        BlockPos bed = player.getSleepingPos().orElse(player.blockPosition());
        return trySpawn(player, wakeChance(player, bed));
    }

    public static float wakeChance(Player player, BlockPos bed) {
        int score = Mth.clamp(sleepQualityScore(player, bed), 0, QUALITY_MAX);
        float t = score / (float) QUALITY_MAX;
        return Mth.lerp(t, WAKE_CHANCE_AT_ZERO, WAKE_CHANCE_AT_TEN);
    }

    public static boolean trySpawn(ServerPlayer player, float chance) {
        if (player == null || player.isSpectator() || !(player.level() instanceof ServerLevel level)) {
            return false;
        }
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return false;
        }
        if (!VansqDebugState.isChaosEnabled() && level.random.nextFloat() >= chance) {
            return false;
        }
        return spawnHerd(level, player) > 0;
    }

    private static int sleepQualityScore(Player player, BlockPos bed) {
        if (player == null || bed == null || !ModList.get().isLoaded("sleepquality")) {
            return 0;
        }
        try {
            Class<?> calculator = Class.forName("net.fudge.sleepquality.util.SleepQualityCalculator");
            Object result = calculator.getMethod("calculateSleepQuality", Player.class, BlockPos.class)
                    .invoke(null, player, bed);
            Object score = result.getClass().getMethod("getScore").invoke(result);
            return score instanceof Integer integer ? integer : 0;
        } catch (ReflectiveOperationException ignored) {
            return 0;
        }
    }

    public static int spawnHerd(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.random;
        int count = MIN_COUNT + random.nextInt(MAX_COUNT - MIN_COUNT + 1);
        BlockPos origin = player.blockPosition();
        List<Goat> spawned = new ArrayList<>();
        int attempts = 0;
        int maxAttempts = count * 32;
        while (spawned.size() < count && attempts < maxAttempts) {
            attempts++;
            int dx = random.nextInt(AREA_HALF * 2 + 1) - AREA_HALF;
            int dz = random.nextInt(AREA_HALF * 2 + 1) - AREA_HALF;
            if (Math.hypot(dx, dz) < MIN_DISTANCE) {
                continue;
            }
            int x = origin.getX() + dx;
            int z = origin.getZ() + dz;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos feet = new BlockPos(x, y, z);
            if (!canStand(level, feet) || !canSeeSkyIgnoringLeaves(level, feet)) {
                continue;
            }
            Goat goat = EntityType.GOAT.create(level);
            if (goat == null) {
                continue;
            }
            goat.moveTo(x + 0.5D, y, z + 0.5D, random.nextFloat() * 360.0F, 0.0F);
            goat.finalizeSpawn(level, level.getCurrentDifficultyAt(feet), MobSpawnType.EVENT, null);
            goat.getPersistentData().putBoolean(TAG_OMEN, true);
            level.addFreshEntity(goat);
            spawned.add(goat);
        }
        if (spawned.isEmpty()) {
            return 0;
        }
        Goat follow = spawned.get(random.nextInt(spawned.size()));
        CompoundTag data = follow.getPersistentData();
        data.putBoolean(TAG_FOLLOW, true);
        data.putUUID(TAG_TARGET, player.getUUID());
        return spawned.size();
    }

    public static void tick(Goat goat) {
        if (!(goat.level() instanceof ServerLevel level) || !isFollow(goat)) {
            return;
        }
        CompoundTag data = goat.getPersistentData();
        Player player = followTarget(level, goat, data);
        if (!data.getBoolean(TAG_ARMED)) {
            if (player == null) {
                return;
            }
            goat.setNoAi(true);
            goat.lookAt(EntityAnchorArgument.Anchor.EYES, player.getEyePosition(1.0F));
            if (isPlayerStaring(player, goat)) {
                int stare = data.getInt(TAG_STARE) + 1;
                data.putInt(TAG_STARE, stare);
                if (stare >= STARE_TICKS) {
                    arm(goat, player);
                }
            } else {
                data.putInt(TAG_STARE, 0);
            }
            return;
        }
        if (goat.isNoAi()) {
            goat.setNoAi(false);
        }
        if (data.getInt(TAG_PHASE) == 0) {
            beginFollow(level, goat, data);
        }
        if (player == null) {
            if (data.getInt(TAG_PHASE) == PHASE_RUSH) {
                vanish(level, goat);
            }
            return;
        }
        if (data.getInt(TAG_PHASE) == PHASE_FOLLOW) {
            int remaining = data.getInt(TAG_PHASE_TICKS) - 1;
            data.putInt(TAG_PHASE_TICKS, remaining);
            if (remaining > 0) {
                goat.getLookControl().setLookAt(player, 30.0F, 30.0F);
                double hold = configDouble("EVENT_CHARGER_FOLLOW_DISTANCE", 8.0D);
                double walk = configDouble("EVENT_CHARGER_WALK_SPEED", 1.0D);
                if (goat.distanceTo(player) > hold) {
                    goat.getNavigation().moveTo(player.getX(), player.getY(), player.getZ(), walk);
                } else {
                    goat.getNavigation().stop();
                }
                return;
            }
            beginRush(level, goat, data);
        }
        int remaining = data.getInt(TAG_PHASE_TICKS) - 1;
        data.putInt(TAG_PHASE_TICKS, remaining);
        if (remaining <= 0) {
            vanish(level, goat);
            return;
        }
        double rush = configDouble("EVENT_CHARGER_RUSH_SPEED", 2.8D);
        goat.getNavigation().moveTo(player.getX(), player.getY(), player.getZ(), rush);
        goat.getLookControl().setLookAt(player, 30.0F, 30.0F);
        if (goat.distanceTo(player) <= HIT_DISTANCE && player instanceof ServerPlayer serverPlayer) {
            resolveHit(level, goat, serverPlayer);
        }
    }

    public static void onGoatKilled(Goat dead, Player killer) {
        if (killer == null || !(dead.level() instanceof ServerLevel level)) {
            return;
        }
        AABB area = dead.getBoundingBox().inflate(KILL_RANGE);
        for (Goat goat : level.getEntitiesOfClass(Goat.class, area)) {
            if (goat.isAlive() && isFollow(goat) && !goat.getPersistentData().getBoolean(TAG_ARMED)) {
                arm(goat, killer);
            }
        }
    }

    public static void discardWithoutFx(Goat goat) {
        goat.discard();
    }

    private static void arm(Goat goat, Player player) {
        CompoundTag data = goat.getPersistentData();
        data.putBoolean(TAG_ARMED, true);
        data.putUUID(TAG_TARGET, player.getUUID());
        goat.setNoAi(false);
    }

    private static void beginFollow(ServerLevel level, Goat goat, CompoundTag data) {
        int min = Mth.clamp(configInt("EVENT_CHARGER_FOLLOW_DURATION_MIN", 30), 1, 600);
        int max = Mth.clamp(configInt("EVENT_CHARGER_FOLLOW_DURATION_MAX", 60), 1, 600);
        if (max < min) {
            int swap = min;
            min = max;
            max = swap;
        }
        int ticks = Mth.floor((min + level.random.nextInt(max - min + 1)) * 20.0D);
        data.putInt(TAG_PHASE, PHASE_FOLLOW);
        data.putInt(TAG_PHASE_TICKS, ticks);
        goat.setSilent(true);
    }

    private static void beginRush(ServerLevel level, Goat goat, CompoundTag data) {
        SoundEvent scream = BuiltInRegistries.SOUND_EVENT.getOptional(RUSH_SCREAMS[level.random.nextInt(RUSH_SCREAMS.length)])
                .orElse(null);
        if (scream != null) {
            level.playSound(null, goat.blockPosition(), scream, SoundSource.HOSTILE, 2.0F, 1.0F);
        }
        data.putInt(TAG_PHASE, PHASE_RUSH);
        data.putInt(TAG_PHASE_TICKS, RUSH_TICKS);
        goat.getNavigation().stop();
    }

    private static void resolveHit(ServerLevel level, Goat goat, ServerPlayer player) {
        float damage = (float) configDouble("EVENT_CHARGER_DAMAGE", 2.0D);
        if (damage > 0.0F) {
            player.hurt(level.damageSources().mobAttack(goat), damage);
        }
        double chance = configDouble("EVENT_CHARGER_GOATMAN_CHANCE", 1.0D);
        if (chance > 0.0D && level.random.nextDouble() < chance) {
            spawnReplacementGoatMan(level, goat, player);
        }
        vanish(level, goat);
    }

    private static void spawnReplacementGoatMan(ServerLevel level, Goat goat, ServerPlayer player) {
        if (!ModList.get().isLoaded("goat_man")) {
            return;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(GOAT_MAN_ID).orElse(null);
        if (type == null) {
            return;
        }
        Entity spawned = type.create(level);
        if (spawned == null) {
            return;
        }
        spawned.moveTo(goat.getX(), goat.getY(), goat.getZ(), goat.getYRot(), 0.0F);
        if (spawned instanceof Mob mob) {
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(goat.blockPosition()), MobSpawnType.TRIGGERED, null);
            mob.setTarget(player);
            mob.setAggressive(true);
        }
        try {
            Class<?> entity = Class.forName(ENTITY_CLASS);
            entity.getField("hasSpawned").setBoolean(spawned, true);
            @SuppressWarnings({"rawtypes", "unchecked"})
            Class<? extends Enum> roll = (Class<? extends Enum>) Class.forName(ROLL_CLASS);
            entity.getField("currentRoll").set(spawned, Enum.valueOf(roll, "CHASE"));
        } catch (ReflectiveOperationException ignored) {
            // Still spawn even if chase flags cannot be set.
        }
        level.addFreshEntity(spawned);
    }

    private static void vanish(ServerLevel level, Goat goat) {
        level.sendParticles(
                ParticleTypes.POOF,
                goat.getX(),
                goat.getY() + goat.getBbHeight() * 0.5D,
                goat.getZ(),
                12,
                0.3D,
                0.3D,
                0.3D,
                0.02D
        );
        goat.discard();
    }

    private static Player followTarget(ServerLevel level, Goat goat, CompoundTag data) {
        if (data.hasUUID(TAG_TARGET) && level.getServer() != null) {
            ServerPlayer stored = level.getServer().getPlayerList().getPlayer(data.getUUID(TAG_TARGET));
            if (stored != null && stored.level() == level && stored.isAlive()) {
                return stored;
            }
        }
        return level.getNearestPlayer(goat, FOLLOW_LOOK_RANGE);
    }

    private static double configDouble(String field, double fallback) {
        Object value = configValue(field);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return fallback;
    }

    private static int configInt(String field, int fallback) {
        Object value = configValue(field);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return fallback;
    }

    private static Object configValue(String field) {
        if (!ModList.get().isLoaded("goat_man")) {
            return null;
        }
        try {
            Object holder = Class.forName(CONFIG_CLASS).getField(field).get(null);
            if (holder == null) {
                return null;
            }
            return holder.getClass().getMethod("get").invoke(holder);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static boolean isPlayerStaring(Player player, Goat goat) {
        if (!player.hasLineOfSight(goat)) {
            return false;
        }
        Vec3 look = player.getViewVector(1.0F);
        Vec3 toGoat = goat.getEyePosition().subtract(player.getEyePosition()).normalize();
        return look.dot(toGoat) >= STARE_DOT;
    }

    private static boolean canStand(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        BlockState ground = level.getBlockState(below);
        return ground.isFaceSturdy(level, below, Direction.UP)
                && level.getBlockState(feet).isAir()
                && level.getBlockState(feet.above()).isAir();
    }

    private static boolean canSeeSkyIgnoringLeaves(Level level, BlockPos feet) {
        BlockPos.MutableBlockPos cursor = feet.mutable();
        int max = level.getMaxBuildHeight();
        while (cursor.getY() < max) {
            BlockState state = level.getBlockState(cursor);
            if (!state.is(BlockTags.LEAVES) && !state.propagatesSkylightDown(level, cursor)) {
                return false;
            }
            cursor.move(Direction.UP);
        }
        return true;
    }
}
