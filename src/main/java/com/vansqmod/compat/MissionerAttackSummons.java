package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Replaces the Missionary's hardcoded undead-summon table.
 * Elite (Easy 5% / Normal 10% / Hard 25%): Bruiser 40% / Juggernaut 60%;
 * one mob, or 1–2 on Hard.
 * Normal: Zombie Villager 25%, Restless Spirit 25%, Skeleton 25%, spear cavalry 25%.
 * Counts: Easy 1–3, Normal 2–4, Hard 2–5.
 * Skeletons use the global baby-pair roll, not a Missionary-specific baby entry.
 */
public final class MissionerAttackSummons {

    private static final ResourceLocation DOOR_KNIGHT =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "door_knight");
    private static final ResourceLocation ZOMBIE_BRUISER =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "zombie_bruiser");
    private static final ResourceLocation JUGGERNAUT =
            ResourceLocation.fromNamespaceAndPath("hominid", "juggernaut");
    private static final ResourceLocation RESTLESS_SPIRIT =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "restless_spirit");
    private static final ResourceLocation IRON_SPEAR =
            ResourceLocation.fromNamespaceAndPath("minecraft", "iron_spear");

    private static final double[][] NORMAL_OFFSETS = {
            {2.5D, 0.5D},
            {-2.5D, 0.5D},
            {0.5D, 2.5D},
            {0.5D, -2.5D},
            {-2.5D, -0.5D},
            {2.5D, -0.5D},
            {-0.5D, 2.5D},
            {-0.5D, -2.5D}
    };

    private static final ThreadLocal<AttackState> ATTACK = new ThreadLocal<>();

    private MissionerAttackSummons() {
    }

    public static void beginAttack(double x, double y, double z) {
        ATTACK.set(new AttackState(x, y, z));
    }

    public static void endAttack() {
        ATTACK.remove();
    }

    public static @Nullable Entity spawnReplacement(
            EntityType<?> original,
            ServerLevel level,
            BlockPos pos,
            MobSpawnType spawnType
    ) {
        AttackState state = ATTACK.get();
        if (state == null) {
            if (isEliteRoll(original)) {
                return spawnElite(level, pos, spawnType);
            }
            return spawnNormal(level, pos, spawnType);
        }
        if (state.batchDone) {
            return null;
        }
        state.batchDone = true;
        if (level.random.nextDouble() < eliteChance(level)) {
            return spawnEliteBatch(level, state, spawnType);
        }
        return spawnNormalBatch(level, state, spawnType);
    }

    private static double eliteChance(ServerLevel level) {
        return switch (level.getDifficulty()) {
            case EASY, PEACEFUL -> 0.05D;
            case HARD -> 0.25D;
            default -> 0.10D;
        };
    }

    private static boolean isEliteRoll(EntityType<?> original) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(original);
        return DOOR_KNIGHT.equals(id) || ZOMBIE_BRUISER.equals(id);
    }

    private static int eliteCount(ServerLevel level) {
        return level.getDifficulty() == Difficulty.HARD
                ? 1 + level.random.nextInt(2)
                : 1;
    }

    private static int normalCount(ServerLevel level) {
        Difficulty difficulty = level.getDifficulty();
        if (difficulty == Difficulty.EASY || difficulty == Difficulty.PEACEFUL) {
            return 1 + level.random.nextInt(3);
        }
        if (difficulty == Difficulty.HARD) {
            return 2 + level.random.nextInt(4);
        }
        return 2 + level.random.nextInt(3);
    }

    private static @Nullable Entity spawnElite(ServerLevel level, BlockPos pos, MobSpawnType spawnType) {
        ResourceLocation primary = level.random.nextDouble() < 0.40D ? ZOMBIE_BRUISER : JUGGERNAUT;
        ResourceLocation fallback = primary.equals(ZOMBIE_BRUISER) ? JUGGERNAUT : ZOMBIE_BRUISER;
        Entity spawned = spawn(primary, level, pos, spawnType);
        return spawned != null ? spawned : spawn(fallback, level, pos, spawnType);
    }

    private static @Nullable Entity spawnEliteBatch(ServerLevel level, AttackState state, MobSpawnType spawnType) {
        return spawnAround(level, state, spawnType, eliteCount(level), MissionerAttackSummons::spawnElite);
    }

    private static @Nullable Entity spawnNormalBatch(ServerLevel level, AttackState state, MobSpawnType spawnType) {
        return spawnAround(level, state, spawnType, normalCount(level), MissionerAttackSummons::spawnNormal);
    }

    @FunctionalInterface
    private interface SummonAt {
        @Nullable Entity spawn(ServerLevel level, BlockPos pos, MobSpawnType spawnType);
    }

    private static @Nullable Entity spawnAround(
            ServerLevel level,
            AttackState state,
            MobSpawnType spawnType,
            int wanted,
            SummonAt summon
    ) {
        List<double[]> offsets = shuffledOffsets(level);
        Entity first = null;
        int spawned = 0;
        for (double[] offset : offsets) {
            if (spawned >= wanted) {
                break;
            }
            BlockPos pos = BlockPos.containing(state.x + offset[0], state.y, state.z + offset[1]);
            Entity entity = summon.spawn(level, pos, spawnType);
            if (entity != null) {
                if (first == null) {
                    first = entity;
                }
                spawned++;
            }
        }
        return first;
    }

    private static List<double[]> shuffledOffsets(ServerLevel level) {
        List<double[]> offsets = new ArrayList<>(NORMAL_OFFSETS.length);
        Collections.addAll(offsets, NORMAL_OFFSETS);
        for (int i = offsets.size() - 1; i > 0; i--) {
            int j = level.random.nextInt(i + 1);
            Collections.swap(offsets, i, j);
        }
        return offsets;
    }

    private static @Nullable Entity spawnNormal(ServerLevel level, BlockPos pos, MobSpawnType spawnType) {
        double roll = level.random.nextDouble();
        if (roll < 0.25D) {
            return EntityType.ZOMBIE_VILLAGER.spawn(level, pos, spawnType);
        }
        if (roll < 0.50D) {
            return spawnOrVillager(RESTLESS_SPIRIT, level, pos, spawnType);
        }
        if (roll < 0.75D) {
            return EntityType.SKELETON.spawn(level, pos, spawnType);
        }
        return spawnSpearCavalry(level, pos, spawnType);
    }

    private static @Nullable Entity spawnOrVillager(
            ResourceLocation id,
            ServerLevel level,
            BlockPos pos,
            MobSpawnType spawnType
    ) {
        Entity spawned = spawn(id, level, pos, spawnType);
        return spawned != null ? spawned : EntityType.ZOMBIE_VILLAGER.spawn(level, pos, spawnType);
    }

    /**
     * Matches Barched's natural zombie-horse trap: rider holding {@code minecraft:iron_spear}
     * on a zombie horse. Uses {@link MobSpawnType#MOB_SUMMONED} so Barched does not also
     * attach a vanilla zombie rider.
     */
    private static @Nullable Entity spawnSpearCavalry(ServerLevel level, BlockPos pos, MobSpawnType spawnType) {
        Entity horse = EntityType.ZOMBIE_HORSE.spawn(level, pos, spawnType);
        Entity rider = EntityType.ZOMBIE_VILLAGER.spawn(level, pos, spawnType);
        if (horse == null && rider == null) {
            return null;
        }
        if (horse == null) {
            equipIronSpear(rider);
            return rider;
        }
        if (rider == null) {
            horse.discard();
            return EntityType.ZOMBIE_VILLAGER.spawn(level, pos, spawnType);
        }

        if (horse instanceof Mob persistentHorse) {
            persistentHorse.setPersistenceRequired();
        }
        if (rider instanceof Mob persistentRider) {
            persistentRider.setPersistenceRequired();
        }
        equipIronSpear(rider);
        if (rider instanceof Mob mob) {
            mob.startRiding(horse, true);
        } else {
            rider.startRiding(horse);
        }
        return rider;
    }

    private static void equipIronSpear(@Nullable Entity rider) {
        if (!(rider instanceof Mob mob)) {
            return;
        }
        BuiltInRegistries.ITEM.getOptional(IRON_SPEAR).ifPresentOrElse(
                item -> mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(item)),
                () -> VansqMod.LOGGER.warn("minecraft:iron_spear is missing; Missionary cavalry rider is unarmed")
        );
    }

    private static @Nullable Entity spawn(
            ResourceLocation id,
            ServerLevel level,
            BlockPos pos,
            MobSpawnType spawnType
    ) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
        return type == null ? null : type.spawn(level, pos, spawnType);
    }

    private static final class AttackState {
        private final double x;
        private final double y;
        private final double z;
        private boolean batchDone;

        private AttackState(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
