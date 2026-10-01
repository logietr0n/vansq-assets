package com.vansqmod.mixin.distanthorizons;

import com.vansqmod.client.DhCloudDayNight;
import com.vansqmod.client.DhCloudOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sky clouds are drawn only in the later transparent layer, so their opacity
 * can stay below fully solid. The DH pass would flatten that alpha to 1.
 */
@Mixin(
        targets = "com.seibel.distanthorizons.core.render.renderer.RenderableBoxGroup",
        remap = false
)
public abstract class RenderableBoxGroupCloudPassMixin {

    @Shadow(remap = false)
    public String resourceLocationPath;

    @Shadow(remap = false)
    public boolean active;

    @Inject(method = "preRender", at = @At("RETURN"), remap = false)
    private void vansqmod$splitCloudPass(CallbackInfo ci) {
        if (!"Clouds".equals(this.resourceLocationPath)) {
            if (DhCloudOverlay.isOverlayPass()) {
                this.active = false;
            }
            return;
        }

        boolean layered = DhCloudDayNight.usesLayer();
        if (DhCloudOverlay.isOverlayPass()) {
            if (!layered) {
                this.active = false;
            }
            return;
        }
        if (layered) {
            this.active = false;
        }
    }
}
