package com.vansqmod.mixin.spideroverhaul;

import com.vansqmod.compat.OceanSpiderLeap;
import dev.chybx.spideroverhaul.entity.OceanSpiderEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.chybx.spideroverhaul.entity.ocean_spider.CrabMoveControl", remap = false)
public abstract class CrabMoveControlMixin {

    @Shadow
    @Final
    private OceanSpiderEntity crab;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void vansqmod$skipDuringLeap(CallbackInfo ci) {
        if (this.crab.isLeaping() || OceanSpiderLeap.isDescending(this.crab)) {
            ci.cancel();
        }
    }

    @ModifyConstant(method = "tick", constant = @Constant(floatValue = 1.25f))
    private float vansqmod$sameSpeedInWater(float original) {
        return 1.0F;
    }

    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = -0.005))
    private double vansqmod$fasterPathSink(double original) {
        return original * OceanSpiderLeap.SINK_GRAVITY_SCALE;
    }
}
