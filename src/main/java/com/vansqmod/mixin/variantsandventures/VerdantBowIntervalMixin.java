package com.vansqmod.mixin.variantsandventures;

import com.vansqmod.compat.VerdantCombat;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Verdant bow cadence matches Bogged (70 / 50 on Hard). Applied on {@link AbstractSkeleton}
 * so it still wins if Verdant does not declare its own interval methods.
 */
@Mixin(AbstractSkeleton.class)
public abstract class VerdantBowIntervalMixin {

    @Inject(method = "getAttackInterval", at = @At("HEAD"), cancellable = true)
    private void vansqmod$verdantBoggedInterval(CallbackInfoReturnable<Integer> cir) {
        if (VerdantCombat.isVerdant((AbstractSkeleton) (Object) this)) {
            cir.setReturnValue(VerdantCombat.ATTACK_INTERVAL);
        }
    }

    @Inject(method = "getHardAttackInterval", at = @At("HEAD"), cancellable = true)
    private void vansqmod$verdantBoggedHardInterval(CallbackInfoReturnable<Integer> cir) {
        if (VerdantCombat.isVerdant((AbstractSkeleton) (Object) this)) {
            cir.setReturnValue(VerdantCombat.HARD_ATTACK_INTERVAL);
        }
    }
}
