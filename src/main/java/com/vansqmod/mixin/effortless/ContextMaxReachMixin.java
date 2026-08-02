package com.vansqmod.mixin.effortless;

import com.vansqmod.integration.effortless.EffortlessReach;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Uses {@link net.minecraft.world.entity.ai.attributes.Attributes#BLOCK_INTERACTION_RANGE} for reach
 * in non-Creative modes; Creative keeps the Effortless config max reach distance.
 */
@Mixin(targets = "dev.huskuraft.effortless.building.Context", remap = false)
public abstract class ContextMaxReachMixin {

    @Inject(method = "maxReachDistance", at = @At("HEAD"), cancellable = true)
    private void vansqmod$attributeBasedReach(CallbackInfoReturnable<Integer> cir) {
        EffortlessReach.setTraceContext(this);
        cir.setReturnValue(EffortlessReach.resolveReach(this));
    }

    @Inject(method = "maxReachDistance", at = @At("RETURN"))
    private void vansqmod$clearTraceContext(CallbackInfoReturnable<Integer> cir) {
        EffortlessReach.clearTraceContext();
    }

    @Inject(method = "maxNextReachDistance", at = @At("HEAD"), cancellable = true)
    private void vansqmod$attributeBasedNextReach(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(EffortlessReach.resolveReach(this));
    }
}
