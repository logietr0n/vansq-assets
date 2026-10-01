package com.vansqmod.mixin.leafculling;

import com.vansqmod.compat.SodiumLeafCulling;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import toni.sodiumleafculling.LeafCullingQuality;

@Mixin(
        targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache",
        remap = false
)
public class SodiumLeafCullingBlockOcclusionCacheMixin {

    @Inject(
            method = "shouldDrawSide",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;skipRendering(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true,
            remap = false
    )
    private void vansqmod$cullLeafFaces(
            BlockState selfState,
            BlockGetter view,
            BlockPos selfPos,
            Direction facing,
            CallbackInfoReturnable<Boolean> cir,
            BlockPos.MutableBlockPos otherPos,
            BlockState otherState
    ) {
        if (!(selfState.getBlock() instanceof LeavesBlock)) {
            return;
        }
        LeafCullingQuality quality = SodiumLeafCulling.quality();
        if (quality == LeafCullingQuality.HOLLOW && SodiumLeafCulling.shouldCullSide(view, selfPos, facing, 2)) {
            cir.setReturnValue(false);
            return;
        }
        if (!(otherState.getBlock() instanceof LeavesBlock) || !quality.isSolid()) {
            return;
        }
        boolean cullSelf = SodiumLeafCulling.surroundedByLeaves(view, selfPos);
        boolean cullOther = SodiumLeafCulling.surroundedByLeaves(view, otherPos);
        if ((!cullSelf && cullOther) || (cullSelf && cullOther)) {
            cir.setReturnValue(false);
        }
    }
}
