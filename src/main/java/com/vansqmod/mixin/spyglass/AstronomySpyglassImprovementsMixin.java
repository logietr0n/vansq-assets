package com.vansqmod.mixin.spyglass;

import me.juancarloscp52.spyglass_improvements.client.SpyglassImprovementsClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Spyglass Astronomy resets edit modes unless a spyglass is in a hand. Spyglass Improvements'
 * zoom keybind can scope via {@code force_spyglass} without putting one in hand — treat that as
 * holding so constellation/star modes work for both hand-use and the zoom keybind.
 */
@Mixin(targets = "com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient", remap = false)
public abstract class AstronomySpyglassImprovementsMixin {

    /** Pack / 1.21 Astronomy (e.g. 1.0.13): positive check. */
    @Inject(method = "isHoldingSpyglass", at = @At("RETURN"), cancellable = true, require = 0)
    private static void vansqmod$holdingOrForceZoom(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && SpyglassImprovementsClient.force_spyglass) {
            cir.setReturnValue(true);
        }
    }

    /** Newer Astronomy (e.g. 26.x source): inverted check. */
    @Inject(method = "isntHoldingSpyglass", at = @At("RETURN"), cancellable = true, require = 0)
    private static void vansqmod$isntHoldingUnlessForceZoom(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && SpyglassImprovementsClient.force_spyglass) {
            cir.setReturnValue(false);
        }
    }
}
