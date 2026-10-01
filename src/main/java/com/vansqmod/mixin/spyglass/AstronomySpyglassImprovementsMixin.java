package com.vansqmod.mixin.spyglass;

import com.vansqmod.client.SpyglassZooming;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Spyglass Astronomy treats a missing hand spyglass as "not holding". Spyglass Improvements'
 * zoom keybind and the Curios spyglass slot never put one in hand — count those as holding.
 */
@Mixin(targets = "com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient", remap = false)
public abstract class AstronomySpyglassImprovementsMixin {

    /** Pack / 1.21 Astronomy (e.g. 1.0.13): positive check. */
    @Inject(method = "isHoldingSpyglass", at = @At("RETURN"), cancellable = true, require = 0)
    private static void vansqmod$holdingOrForceZoom(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && SpyglassZooming.isHoldingOrWearingSpyglass()) {
            cir.setReturnValue(true);
        }
    }

    /** Newer Astronomy (e.g. 26.x source): inverted check. */
    @Inject(method = "isntHoldingSpyglass", at = @At("RETURN"), cancellable = true, require = 0)
    private static void vansqmod$isntHoldingUnlessForceZoom(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && SpyglassZooming.isHoldingOrWearingSpyglass()) {
            cir.setReturnValue(false);
        }
    }
}
