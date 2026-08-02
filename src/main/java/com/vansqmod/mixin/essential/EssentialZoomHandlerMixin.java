package com.vansqmod.mixin.essential;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Essential has no master zoom toggle. Stub the zoom handler so the keybind and scroll zoom do nothing.
 */
@Mixin(targets = "gg.essential.handlers.ZoomHandler", remap = false)
public abstract class EssentialZoomHandlerMixin {

    @Inject(method = "getZoomState", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$disableZoomState(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "applyModifiers", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$disableZoomFov(float modifier, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(modifier);
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$disableZoomScroll(@Coerce Object event, CallbackInfo ci) {
        ci.cancel();
    }
}
