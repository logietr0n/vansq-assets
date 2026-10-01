package com.vansqmod.mixin.astronomy;

import com.vansqmod.client.AstronomyClientSync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.nettakrim.spyglass_astronomy.SpaceDataManager", remap = false)
public abstract class AstronomyMakeChangeMixin {

    @Inject(method = "makeChange", at = @At("RETURN"))
    private static void vansqmod$uploadWorldAstronomy(CallbackInfo ci) {
        AstronomyClientSync.onLocalChange();
    }
}
