package com.vansqmod.mixin.distanthorizons;

import com.seibel.distanthorizons.api.enums.config.EDhApiDepthDirection;
import com.seibel.distanthorizons.api.enums.config.EDhApiDepthRange;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiFogRenderParam;
import com.seibel.distanthorizons.core.config.Config;
import com.seibel.distanthorizons.core.render.RenderParams;
import com.seibel.distanthorizons.core.util.math.DhMat4f;
import com.seibel.distanthorizons.core.wrapperInterfaces.render.AbstractDhRenderApiDefinition;
import com.vansqmod.client.DhCloudFog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Remembers the fog Distant Horizons is about to draw so the later cloud
 * layer can use it. This only reads Java fields. It does not query or edit
 * the fog shader's GL uniforms.
 */
@Mixin(
        targets = "com.seibel.distanthorizons.common.render.openGl.postProcessing.fog.GlDhFogShader_neoforge",
        remap = false
)
public abstract class GlDhFogShaderCloudFogMixin {

    @Shadow(remap = false)
    private static AbstractDhRenderApiDefinition RENDER_DEF;

    @Shadow(remap = false)
    private DhMat4f inverseMvmProjMatrix;

    @Shadow(remap = false)
    private DhApiFogRenderParam fogRenderParams;

    @Inject(method = "onApplyUniforms", at = @At("RETURN"), remap = false)
    private void vansqmod$captureCloudFog(RenderParams renderParams, CallbackInfo ci) {
        try {
            if (this.fogRenderParams == null || this.inverseMvmProjMatrix == null || renderParams.clientLevelWrapper == null || RENDER_DEF == null) {
                return;
            }
            int chunkRadius = (Integer) Config.Client.Advanced.Graphics.Quality.lodChunkRenderDistanceRadius.get();
            float farBlocks = Math.max(1, chunkRadius * 16);
            int worldHeight = Math.max(1, renderParams.clientLevelWrapper.getMaxHeight());
            DhCloudFog.capture(
                    this.inverseMvmProjMatrix,
                    this.fogRenderParams,
                    1.0F / farBlocks,
                    1.0F / worldHeight,
                    RENDER_DEF.getDepthDirection() == EDhApiDepthDirection.REVERSE_Z,
                    RENDER_DEF.getDepthRange() == EDhApiDepthRange.ZERO_TO_POS_ONE,
                    (float) renderParams.exactCameraPosition.y
            );
        } catch (Throwable ignored) {
            // A failed copy must not abort Distant Horizons' own fog draw.
        }
    }
}
