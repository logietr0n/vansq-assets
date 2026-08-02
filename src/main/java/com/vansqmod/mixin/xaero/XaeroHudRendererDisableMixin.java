package com.vansqmod.mixin.xaero;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppress Xaero's on-screen HUD (minimap / radar modules).
 */
@Mixin(targets = "xaero.hud.render.HudRenderer", remap = false)
public class XaeroHudRendererDisableMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void vansqmod$disableXaeroHud(CallbackInfo ci) {
        ci.cancel();
    }
}
