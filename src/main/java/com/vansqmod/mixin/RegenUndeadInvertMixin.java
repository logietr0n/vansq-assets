package com.vansqmod.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Regeneration harms undead the same way Instant Health does, at regener's usual 1 HP tick rate.
 * Keeps poison's floor so it will not finish the mob off.
 */
@Mixin(targets = "net.minecraft.world.effect.RegenerationMobEffect")
public abstract class RegenUndeadInvertMixin {

    @Inject(method = "applyEffectTick", at = @At("HEAD"), cancellable = true)
    private void vansqmod$harmUndead(
            LivingEntity living,
            int amplifier,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!living.isInvertedHealAndHarm()) {
            return;
        }
        if (living.getHealth() > 1.0F) {
            living.hurt(living.damageSources().magic(), 1.0F);
        }
        cir.setReturnValue(true);
    }
}
