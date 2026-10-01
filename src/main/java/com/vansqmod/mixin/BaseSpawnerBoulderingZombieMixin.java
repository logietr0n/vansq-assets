package com.vansqmod.mixin;

import com.vansqmod.entity.BoulderingZombieSpawns;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * Vanilla (and dungeon) zombie spawners below y=0 become Bouldering Zombie spawners.
 * Variants and Ventures remaps some dungeon spawners first via its own dungeon mixin;
 * this only rewrites leftover {@code minecraft:zombie} IDs.
 */
@Mixin(BaseSpawner.class)
public abstract class BaseSpawnerBoulderingZombieMixin implements BoulderingZombieSpawns.BaseSpawnerAccess {

    @Shadow
    @Nullable
    private SpawnData nextSpawnData;

    @Override
    public SpawnData vansqmod$getNextSpawnData() {
        return this.nextSpawnData;
    }

    @ModifyVariable(method = "setEntityId", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private EntityType<?> vansqmod$remapZombieUnderY0(
            EntityType<?> type,
            EntityType<?> typeArg,
            @Nullable Level level,
            RandomSource random,
            BlockPos pos
    ) {
        return BoulderingZombieSpawns.remapSpawnerType(type, pos);
    }

    @Inject(method = "serverTick", at = @At("HEAD"))
    private void vansqmod$remapLoadedZombieSpawner(ServerLevel level, BlockPos pos, CallbackInfo ci) {
        BoulderingZombieSpawns.remapSpawner((BaseSpawner) (Object) this, level, level.random, pos);
    }

    @Inject(method = "clientTick", at = @At("HEAD"))
    private void vansqmod$remapLoadedZombieSpawnerClient(Level level, BlockPos pos, CallbackInfo ci) {
        BoulderingZombieSpawns.remapSpawner((BaseSpawner) (Object) this, level, level.random, pos);
    }
}
