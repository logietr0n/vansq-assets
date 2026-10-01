package com.vansqmod.mixin.essential;

import com.vansqmod.compat.EssentialFancyMenuLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Pause-menu overlay init happens on first draw, after screen {@code init}. Rebind
 * FancyMenu positions once that overlay exists so Essential follows the layout.
 */
@Mixin(targets = "gg.essential.handlers.PauseMenuDisplay", remap = false)
public abstract class EssentialPauseMenuFancyMenuMixin {

    @Inject(
            method = "initContent(Lnet/minecraft/client/gui/screens/Screen;)V",
            at = @At("RETURN"),
            remap = false
    )
    private void vansqmod$rebindAfterOverlay(@Coerce Object screen, CallbackInfo ci) {
        EssentialFancyMenuLayout.rebindCurrentScreen();
    }

    @Inject(
            method = "initContent(Lnet/minecraft/client/gui/screens/Screen;Lgg/essential/elementa/components/Window;Lgg/essential/gui/proxies/ScreenWithProxiesHandler;)V",
            at = @At("RETURN"),
            remap = false
    )
    private void vansqmod$rebindAfterOverlayLayout(@Coerce Object screen, @Coerce Object window, @Coerce Object handler, CallbackInfo ci) {
        EssentialFancyMenuLayout.rebindCurrentScreen();
    }
}
