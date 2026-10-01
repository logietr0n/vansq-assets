package com.vansqmod.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla blocks Poison and Regeneration on {@code #minecraft:ignores_poison_and_regen}
 * (all undead). Allow them so inverted ticks can run, matching Instant Health / Damage.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityPoisonRegenUndeadMixin {

    @Inject(method = "canBeAffected", at = @At("HEAD"), cancellable = true)
    private void vansqmod$undeadCanReceivePoisonRegen(
            MobEffectInstance effect,
            CallbackInfoReturnable<Boolean> cir
    ) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.isInvertedHealAndHarm()) {
            return;
        }
        Holder<MobEffect> holder = effect.getEffect();
        if (holder.is(MobEffects.POISON) || holder.is(MobEffects.REGENERATION)) {
            cir.setReturnValue(true);
        }
    }
}
