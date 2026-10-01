package com.vansqmod.mixin.essential;

import com.vansqmod.compat.EssentialFancyMenuLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * After Essential rebuilds title/pause/options proxy buttons, point FancyMenu's
 * layout entries at the new widgets so custom positions survive returning to the menu.
 */
@Mixin(targets = "gg.essential.gui.proxies.ScreenWithProxiesHandler", remap = false)
public abstract class EssentialProxyInitGuiMixin {

    @Inject(method = "initGui", at = @At("RETURN"), remap = false)
    private void vansqmod$rebindFancyMenuButtons(CallbackInfo ci) {
        EssentialFancyMenuLayout.rebindFromProxyHandler(this);
    }
}
