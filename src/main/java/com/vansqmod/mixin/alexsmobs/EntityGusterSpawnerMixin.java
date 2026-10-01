package com.vansqmod.mixin.alexsmobs;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Alex's Mobs only lets Gusters spawn on {@code #minecraft:sand} during rain/thunder.
 * Monster-room spawners sit on ancient sandstone underground, so those checks never pass.
 * Spawner (and trial-spawner) attempts skip them; natural desert weather spawning is unchanged.
 */
@Mixin(targets = "com.github.alexthe666.alexsmobs.entity.EntityGuster", remap = false)
public abstract class EntityGusterSpawnerMixin {

    @Inject(method = "canGusterSpawn", at = @At("HEAD"), cancellable = true, remap = false)
    private static void vansqmod$allowSpawnerGusters(
            EntityType<?> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (spawnType == MobSpawnType.SPAWNER || spawnType == MobSpawnType.TRIAL_SPAWNER) {
            cir.setReturnValue(true);
        }
    }
}
