package com.vansqmod.mixin.essential;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * GuiUtil.queueModal is the pause-menu path into Essential's overlay. Block update
 * modals here as well as on ModalManagerImpl.
 */
@Mixin(targets = "gg.essential.util.GuiUtil", remap = false)
public abstract class EssentialGuiUtilModalMixin {

    @Inject(method = "queueModal", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$blockUpdateModals(@Coerce Object modal, CallbackInfo ci) {
        if (vansqmod$isEssentialUpdateModal(modal)) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean vansqmod$isEssentialUpdateModal(Object modal) {
        if (modal == null) {
            return false;
        }
        String name = modal.getClass().getName();
        if (!name.startsWith("gg.essential.")) {
            return false;
        }
        int dot = name.lastIndexOf('.');
        String simple = dot >= 0 ? name.substring(dot + 1) : name;
        return simple.contains("AutoInstalled")
                || (simple.contains("Update") && simple.contains("Modal"));
    }
}
