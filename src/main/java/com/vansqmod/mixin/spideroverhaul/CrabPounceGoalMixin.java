package com.vansqmod.mixin.spideroverhaul;

import com.vansqmod.compat.OceanSpiderLeap;
import dev.chybx.spideroverhaul.entity.OceanSpiderEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.chybx.spideroverhaul.entity.ocean_spider.CrabPounceGoal", remap = false)
public abstract class CrabPounceGoalMixin {

    @Shadow
    @Final
    private OceanSpiderEntity crab;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void vansqmod$waterLeapRange(CallbackInfoReturnable<Boolean> cir) {
        if (!this.crab.isInWater()) {
            return;
        }
        cir.setReturnValue(OceanSpiderLeap.canStartWaterLeap(this.crab));
    }

    @Inject(method = "start", at = @At("HEAD"), cancellable = true)
    private void vansqmod$waterLeapStart(CallbackInfo ci) {
        if (!this.crab.isInWater()) {
            return;
        }
        OceanSpiderLeap.startWaterLeap(this.crab);
        ci.cancel();
    }
}
