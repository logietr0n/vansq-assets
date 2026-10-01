package com.vansqmod.mixin.essential;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drops Essential update modals before they are shown.
 * Logic is inlined: this handler is copied into Essential's classloader, which cannot
 * see {@code com.vansqmod.compat} classes.
 */
@Mixin(targets = "gg.essential.gui.overlay.ModalManagerImpl", remap = false)
public abstract class EssentialModalManagerImplMixin {

    @Inject(method = "queueModal", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$blockUpdateModals(@Coerce Object modal, CallbackInfo ci) {
        if (vansqmod$isUpdateModal(modal)) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean vansqmod$isUpdateModal(Object modal) {
        if (modal == null) {
            return false;
        }
        return vansqmod$isEssentialUpdateModal(modal.getClass().getName());
    }

    @Unique
    private static boolean vansqmod$isEssentialUpdateModal(String name) {
        if (name == null || !name.startsWith("gg.essential.")) {
            return false;
        }
        int dot = name.lastIndexOf('.');
        String simple = dot >= 0 ? name.substring(dot + 1) : name;
        return simple.contains("AutoInstalled")
                || (simple.contains("Update") && simple.contains("Modal"));
    }
}
