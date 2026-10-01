package com.vansqmod.mixin.essential;

import com.vansqmod.compat.EssentialFancyMenuLayout;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Essential's overlay follows FancyMenu-moved proxies only while {@code proxyInControl}
 * is true. Re-attaching the overlay always clears that flag, which is why the real
 * Essential buttons snap back to the default sidebar after leaving the title screen.
 */
@Mixin(targets = "gg.essential.gui.proxies.EssentialProxyElement", remap = false)
public abstract class EssentialProxyFancyMenuControlMixin {

    @Inject(method = "updateProxyState", at = @At("HEAD"), remap = false)
    private void vansqmod$followFancyMenu(CallbackInfo ci) {
        EssentialFancyMenuLayout.takeControlIfCustomized((AbstractWidget) (Object) this);
    }

    @Inject(method = "acceptNewEssentialContainer", at = @At("RETURN"), remap = false)
    private void vansqmod$keepFancyMenuControlAfterAttach(@Coerce Object container, @Coerce Object mounting, CallbackInfo ci) {
        EssentialFancyMenuLayout.takeControlIfCustomized((AbstractWidget) (Object) this);
    }
}
