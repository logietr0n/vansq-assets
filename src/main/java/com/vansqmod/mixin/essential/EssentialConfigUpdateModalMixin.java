package com.vansqmod.mixin.essential;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Forces Essential's {@code update_notifications} gate off even if the saved setting is ignored.
 */
@Mixin(targets = "gg.essential.config.EssentialConfig", remap = false)
public abstract class EssentialConfigUpdateModalMixin {

    @Inject(method = "getUpdateModal", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$forceUpdateModalOff(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
