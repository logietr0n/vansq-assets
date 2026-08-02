package com.vansqmod.mixin.xaero;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Block Xaero's World Map keybinds (open map / settings / etc.).
 */
@Mixin(targets = "xaero.map.controls.ControlsHandler", remap = false)
public class XaeroWorldMapControlsDisableMixin {

    @Inject(method = "keyDown", at = @At("HEAD"), cancellable = true)
    private void vansqmod$disableWorldMapKeys(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "handleKeyEvents", at = @At("HEAD"), cancellable = true)
    private void vansqmod$disableWorldMapKeyEvents(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onKeyInput", at = @At("HEAD"), cancellable = true)
    private void vansqmod$disableWorldMapKeyInput(CallbackInfo ci) {
        ci.cancel();
    }
}
