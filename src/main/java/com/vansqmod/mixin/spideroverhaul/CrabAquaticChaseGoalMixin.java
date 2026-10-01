package com.vansqmod.mixin.spideroverhaul;

import com.vansqmod.compat.OceanSpiderLeap;
import dev.chybx.spideroverhaul.entity.OceanSpiderEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.chybx.spideroverhaul.entity.ocean_spider.CrabAquaticChaseGoal", remap = false)
public abstract class CrabAquaticChaseGoalMixin {

    @Shadow
    @Final
    private OceanSpiderEntity crab;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void vansqmod$noChaseDuringLeap(CallbackInfoReturnable<Boolean> cir) {
        if (this.crab.isLeaping() || OceanSpiderLeap.isDescending(this.crab)) {
            cir.setReturnValue(false);
        }
    }
}
