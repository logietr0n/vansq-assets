package com.vansqmod.mixin.astronomy;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.nettakrim.spyglass_astronomy.commands.SelectCommand", remap = false)
public abstract class AstronomySelectCommandMixin {

    @Inject(method = "selectStar", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$blockStarSelect(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(-1);
    }
}
