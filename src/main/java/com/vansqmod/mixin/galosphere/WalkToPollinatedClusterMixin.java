package com.vansqmod.mixin.galosphere;

import com.vansqmod.compat.GalosphereSparkleCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.orcinus.galosphere.entities.Sparkle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.orcinus.galosphere.entities.ai.tasks.sparkle.WalkToPollinatedCluster", remap = false)
public class WalkToPollinatedClusterMixin {

    @Shadow
    private boolean setCooldownOnly;

    @Inject(method = "isPollinatedCluster", at = @At("HEAD"), cancellable = true)
    private void vansqmod$acceptRhodoheartCluster(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (GalosphereSparkleCompat.isUnpollinatedRhodoheartCluster(state)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "stop(Lnet/minecraft/server/level/ServerLevel;Lnet/orcinus/galosphere/entities/Sparkle;J)V",
            at = @At("HEAD"),
            remap = false
    )
    private void vansqmod$capturePollinationTarget(ServerLevel world, Sparkle entity, long gameTime, CallbackInfo ci) {
        GalosphereSparkleCompat.capturePollinationTarget(entity);
    }

    @Inject(
            method = "stop(Lnet/minecraft/server/level/ServerLevel;Lnet/orcinus/galosphere/entities/Sparkle;J)V",
            at = @At("TAIL"),
            remap = false
    )
    private void vansqmod$finishPollination(ServerLevel world, Sparkle entity, long gameTime, CallbackInfo ci) {
        GalosphereSparkleCompat.finishPollination(world, entity, this.setCooldownOnly);
    }

    @Redirect(
            method = "lambda$stop$2",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;withPropertiesOf(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;"
            )
    )
    private BlockState vansqmod$buildGlintedClusterState(
            Block placeState,
            BlockState clusterState,
            Sparkle entity,
            ServerLevel world,
            BlockPos pos
    ) {
        BlockState resolved = GalosphereSparkleCompat.buildGlintedClusterState(clusterState);
        if (resolved != clusterState) {
            return resolved;
        }
        if (placeState != null) {
            return placeState.withPropertiesOf(clusterState);
        }
        return clusterState;
    }
}
