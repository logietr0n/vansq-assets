package com.vansqmod.mixin.astronomy;

import com.vansqmod.client.AstronomyClientSync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient", remap = false)
public abstract class AstronomyLoadSpaceMixin {

    @Inject(method = "loadSpace", at = @At("RETURN"))
    private static void vansqmod$applyWorldAstronomy(CallbackInfo ci) {
        AstronomyClientSync.onSpaceLoaded();
    }
}
