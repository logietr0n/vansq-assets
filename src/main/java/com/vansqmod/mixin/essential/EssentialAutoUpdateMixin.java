package com.vansqmod.mixin.essential;

import com.vansqmod.compat.EssentialUpdateModals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Short-circuits Essential's update modal flow so {@code ModalFlow.awaitModal} never hangs
 * when queueing is cancelled.
 */
@Mixin(targets = "gg.essential.util.AutoUpdate", remap = false)
public abstract class EssentialAutoUpdateMixin {

    @Inject(method = "showUpdateModal", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$skipUpdateModal(
            @Coerce Object modalFlow,
            @Coerce Object continuation,
            CallbackInfoReturnable<Object> cir
    ) {
        cir.setReturnValue(EssentialUpdateModals.kotlinUnit());
    }

    @Inject(method = "requiresUpdate", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$neverRequiresUpdate(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
