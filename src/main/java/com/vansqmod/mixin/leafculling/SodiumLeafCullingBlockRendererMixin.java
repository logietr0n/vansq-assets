package com.vansqmod.mixin.leafculling;

import com.vansqmod.compat.SodiumLeafCulling;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.world.level.block.LeavesBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(
        targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer",
        remap = false
)
public abstract class SodiumLeafCullingBlockRendererMixin {

    @ModifyVariable(method = "processQuad", at = @At(value = "STORE"), remap = false)
    private BlendMode vansqmod$solidInteriorLeaves(BlendMode blendMode) {
        SodiumLeafCullingRenderContextAccessor ctx = (SodiumLeafCullingRenderContextAccessor) (Object) this;
        if (!(ctx.vansqmod$getState().getBlock() instanceof LeavesBlock)) {
            return blendMode;
        }
        if (SodiumLeafCulling.quality().isSolid()
                && SodiumLeafCulling.surroundedByLeaves(ctx.vansqmod$getSlice(), ctx.vansqmod$getPos())) {
            return BlendMode.SOLID;
        }
        return blendMode;
    }
}
