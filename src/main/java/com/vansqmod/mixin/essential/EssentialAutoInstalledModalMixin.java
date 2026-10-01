package com.vansqmod.mixin.essential;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Essential shows "Essential Mod has been installed because its libraries are required"
 * on first launch in some pack/container setups. Force that gate off so {@code showModal}
 * only records the launch flag and never pushes the window.
 */
@Mixin(targets = "gg.essential.gui.modals.EssentialAutoInstalledModal$Companion", remap = false)
public abstract class EssentialAutoInstalledModalMixin {

    @Inject(method = "shouldShowModal", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$neverShowAutoInstalled(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
