package com.vansqmod.mixin.xaero;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Block Xaero's Minimap keybinds (zoom, settings, enlarge, etc.).
 */
@Mixin(targets = "xaero.common.controls.ControlsHandler", remap = false)
public class XaeroMinimapControlsDisableMixin {

    @Inject(method = "keyDown", at = @At("HEAD"), cancellable = true)
    private void vansqmod$disableMinimapKeys(CallbackInfo ci) {
        ci.cancel();
    }
}
