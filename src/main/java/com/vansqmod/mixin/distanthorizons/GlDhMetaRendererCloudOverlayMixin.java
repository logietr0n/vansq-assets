package com.vansqmod.mixin.distanthorizons;

import com.seibel.distanthorizons.common.wrappers.misc.LightMapWrapper_neoforge;
import com.seibel.distanthorizons.core.render.RenderParams;
import com.vansqmod.client.DhCloudOverlay;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Clouds are blended after DH has applied its frame, on top of the sky
 * Spyglass Astronomy already drew.
 */
@Mixin(
        targets = "com.seibel.distanthorizons.common.render.openGl.GlDhMetaRenderer_neoforge",
        remap = false
)
public abstract class GlDhMetaRendererCloudOverlayMixin {

    @Shadow(remap = false)
    public abstract int getActiveDepthTextureId();

    @Inject(method = "runRenderPassSetup", at = @At("RETURN"), remap = false)
    private void vansqmod$noteCloudPass(RenderParams renderParams, CallbackInfo ci) {
        int lightmapId = 0;
        if (renderParams.lightmap instanceof LightMapWrapper_neoforge wrapper) {
            lightmapId = wrapper.getOpenGlId();
        }
        int[] depthFunc = new int[1];
        double[] clearDepth = new double[1];
        double[] depthRange = new double[2];
        GL11.glGetIntegerv(GL11.GL_DEPTH_FUNC, depthFunc);
        GL11.glGetDoublev(GL11.GL_DEPTH_CLEAR_VALUE, clearDepth);
        GL11.glGetDoublev(GL11.GL_DEPTH_RANGE, depthRange);
        DhCloudOverlay.notePassState(lightmapId, depthFunc[0], clearDepth[0], this.getActiveDepthTextureId(), depthRange[0], depthRange[1]);
    }

    @Inject(method = "copyToMcTexture", at = @At("RETURN"), remap = false)
    private void vansqmod$overlayClouds(CallbackInfo ci) {
        DhCloudOverlay.draw();
    }
}
