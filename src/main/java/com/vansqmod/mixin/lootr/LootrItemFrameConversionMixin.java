package com.vansqmod.mixin.lootr;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lootr replaces structure item frames (and End City elytra frames) with its own
 * entity. Keep vanilla item frames; chests and barrels still convert.
 */
@Mixin(targets = "noobanidus.mods.lootr.neoforge.impl.LootrAPIImpl", remap = false)
public abstract class LootrItemFrameConversionMixin {

    @Inject(method = "shouldConvertStructureItemFrames", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$noItemFrameConversion(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "shouldConvertElytrasToItemFrames", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$noElytraItemFrames(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
