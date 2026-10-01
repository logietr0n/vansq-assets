package com.vansqmod.mixin.distanthorizons;

import com.seibel.distanthorizons.core.render.RenderParams;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IProfilerWrapper;
import com.vansqmod.client.DhCloudOverlay;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        targets = "com.seibel.distanthorizons.common.render.openGl.generic.GlGenericObjectRenderer",
        remap = false
)
public abstract class GlGenericObjectRendererCloudOverlayMixin {

    @Shadow(remap = false)
    public abstract void render(RenderParams renderParams, IProfilerWrapper profiler, boolean ssaoPass);

    @Inject(method = "render", at = @At("HEAD"), remap = false)
    private void vansqmod$captureCloudDraw(RenderParams renderParams, IProfilerWrapper profiler, boolean ssaoPass, CallbackInfo ci) {
        DhCloudOverlay.setDraw(() -> this.render(renderParams, profiler, false));
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/seibel/distanthorizons/common/wrappers/minecraft/MinecraftGLWrapper;glBlendFuncSeparate(IIII)V",
                    shift = At.Shift.AFTER
            ),
            remap = false
    )
    private void vansqmod$maxCloudLayer(RenderParams renderParams, IProfilerWrapper profiler, boolean ssaoPass, CallbackInfo ci) {
        if (DhCloudOverlay.isCapturing()) {
            // The nearest shaded face is kept by the depth test. Blending here
            // would stack transparent boxes on top of each other.
            GL11.glDisable(GL11.GL_BLEND);
        }
    }
}
