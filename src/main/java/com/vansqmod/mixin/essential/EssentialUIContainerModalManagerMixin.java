package com.vansqmod.mixin.essential;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Same update-modal filter for Essential's UIContainer-backed modal manager.
 * Inlined so the handler does not load vansqmod classes from Essential's classloader.
 */
@Mixin(targets = "gg.essential.gui.overlay.UIContainerModalManagerImpl", remap = false)
public abstract class EssentialUIContainerModalManagerMixin {

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
