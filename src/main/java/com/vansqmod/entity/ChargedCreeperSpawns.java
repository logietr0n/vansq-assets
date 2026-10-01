package com.vansqmod.entity;

import com.vansqmod.VansqMod;
import com.vansqmod.debug.VansqDebugState;
import com.vansqmod.mixin.CreeperPoweredAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * Vanilla and Creeper Overhaul creepers can spawn charged: 8% during
 * thunderstorms (16% for ocean creepers). On Hard, 1% otherwise; thunder
 * chances replace that roll while the storm is active.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class ChargedCreeperSpawns {

    public static final ResourceLocation OCEAN_CREEPER_ID =
            ResourceLocation.fromNamespaceAndPath("creeperoverhaul", "ocean_creeper");

    public static final float THUNDER_CHANCE = 0.08F;
    public static final float OCEAN_THUNDER_CHANCE = 0.16F;
    public static final float HARD_CHANCE = 0.01F;

    private ChargedCreeperSpawns() {
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (event.isCanceled() || event.isSpawnCancelled()) {
            return;
        }
        if (!shouldChargeSpawnType(event.getSpawnType())) {
            return;
        }
        if (!(event.getEntity() instanceof Creeper creeper) || creeper.isPowered()) {
            return;
        }

        ServerLevelAccessor world = event.getLevel();
        if (world.getLevel().isClientSide()) {
            return;
        }

        if (!VansqDebugState.isForceRareEventsEnabled()) {
            float chance = chargedChance(world, creeper);
            if (chance <= 0.0F || creeper.getRandom().nextFloat() >= chance) {
                return;
            }
        }
        creeper.getEntityData().set(CreeperPoweredAccessor.vansqmod$dataIsPowered(), true);
    }

    public static float chargedChance(ServerLevelAccessor world, Creeper creeper) {
        if (world.getLevel().isThundering()) {
            return isOceanCreeper(creeper) ? OCEAN_THUNDER_CHANCE : THUNDER_CHANCE;
        }
        if (world.getDifficulty() == Difficulty.HARD) {
            return HARD_CHANCE;
        }
        return 0.0F;
    }

    public static boolean isOceanCreeper(Creeper creeper) {
        return OCEAN_CREEPER_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(creeper.getType()));
    }

    private static boolean shouldChargeSpawnType(MobSpawnType spawnType) {
        return spawnType == MobSpawnType.NATURAL
                || spawnType == MobSpawnType.CHUNK_GENERATION
                || spawnType == MobSpawnType.STRUCTURE
                || spawnType == MobSpawnType.SPAWNER
                || spawnType == MobSpawnType.TRIAL_SPAWNER
                || spawnType == MobSpawnType.REINFORCEMENT;
    }
}
