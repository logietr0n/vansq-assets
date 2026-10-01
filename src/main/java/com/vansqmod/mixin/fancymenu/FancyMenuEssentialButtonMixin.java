package com.vansqmod.mixin.fancymenu;

import com.vansqmod.compat.EssentialFancyMenuLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * FancyMenu applies vanilla-button layouts during screen init. Essential often
 * finishes creating its proxies in that same pass, so rebind once FancyMenu is done.
 */
@Mixin(targets = "de.keksuccino.fancymenu.customization.layer.ScreenCustomizationLayer", remap = false)
public abstract class FancyMenuEssentialButtonMixin {

    @Inject(method = "onInitOrResizeScreenPost", at = @At("RETURN"), remap = false)
    private void vansqmod$rebindEssentialButtons(@Coerce Object event, CallbackInfo ci) {
        EssentialFancyMenuLayout.rebindFromFancyMenuEvent(event);
    }
}
