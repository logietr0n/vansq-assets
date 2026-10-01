package com.vansqmod.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Poison heals undead the same way Instant Damage does, at poison's usual 1 HP tick rate.
 */
@Mixin(targets = "net.minecraft.world.effect.PoisonMobEffect")
public abstract class PoisonUndeadInvertMixin {

    @Inject(method = "applyEffectTick", at = @At("HEAD"), cancellable = true)
    private void vansqmod$healUndead(
            LivingEntity living,
            int amplifier,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!living.isInvertedHealAndHarm()) {
            return;
        }
        if (living.getHealth() < living.getMaxHealth()) {
            living.heal(1.0F);
        }
        cir.setReturnValue(true);
    }
}
