package com.vansqmod.mixin.essential;

import com.vansqmod.compat.EssentialUpdateModals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Same update-modal filter for Essential's UIContainer-backed modal manager. */
@Mixin(targets = "gg.essential.gui.overlay.UIContainerModalManagerImpl", remap = false)
public abstract class EssentialUIContainerModalManagerMixin {

    @Inject(method = "queueModal", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$blockUpdateModals(@Coerce Object modal, CallbackInfo ci) {
        if (EssentialUpdateModals.isUpdateModal(modal)) {
            ci.cancel();
        }
    }
}
