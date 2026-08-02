package com.vansqmod.mixin.essential;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The pause-menu "update available" toast is separate from {@code update_notifications} and is
 * what usually still appears after that setting is turned off.
 */
@Mixin(targets = "gg.essential.handlers.PauseMenuDisplay", remap = false)
public abstract class EssentialPauseMenuUpdateToastMixin {

    @Inject(method = "initModals$showUpdateToast", at = @At("HEAD"), cancellable = true, remap = false)
    private static void vansqmod$noUpdateToast(String title, CallbackInfo ci) {
        ci.cancel();
    }
}
