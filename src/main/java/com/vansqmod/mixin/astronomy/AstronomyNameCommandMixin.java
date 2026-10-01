package com.vansqmod.mixin.astronomy;

import com.vansqmod.client.AstronomySpyglassControls;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.nettakrim.spyglass_astronomy.commands.NameCommand", remap = false)
public abstract class AstronomyNameCommandMixin {

    @Inject(method = "run", at = @At("HEAD"))
    private void vansqmod$preferConstellationAndPlanetNames(CallbackInfoReturnable<Integer> cir) {
        AstronomySpyglassControls.deselectStar();
    }

    @Inject(
            method = "name(Lcom/nettakrim/spyglass_astronomy/Star;Ljava/lang/String;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void vansqmod$blockStarNaming(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "nameStar", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$blockStarNameCommand(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(-1);
    }
}
