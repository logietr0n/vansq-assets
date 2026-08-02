package com.vansqmod.mixin.quark;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Disable Quark Pig Litters — vansqmod {@code PigLitters} owns litter size and
 * parent-variant inheritance so extras are not biome-overwritten.
 */
@Mixin(targets = "org.violetmoon.quark.content.tweaks.module.PigLittersModule", remap = false)
public class QuarkPigLittersDisableMixin {

    @Inject(
            method = "onPigBreed(Lorg/violetmoon/zeta/event/play/entity/living/ZBabyEntitySpawn$Lowest;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void vansqmod$disablePigLitters(CallbackInfo ci) {
        ci.cancel();
    }
}
