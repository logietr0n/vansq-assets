package com.vansqmod.mixin.variantsandventures;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Thickets apply Poison II on a successful melee hit (replaces their vanilla Poison I).
 */
@Mixin(targets = "com.faboslav.variantsandventures.common.entity.mob.ThicketEntity")
public abstract class ThicketPoisonMixin {

    private static final int POISON_DURATION_TICKS = 100;
    private static final int POISON_AMPLIFIER = 1;

    @Inject(method = "doHurtTarget", at = @At("RETURN"))
    private void vansqmod$poisonII(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || !(target instanceof LivingEntity living) || living.level().isClientSide) {
            return;
        }
        living.addEffect(
                new MobEffectInstance(MobEffects.POISON, POISON_DURATION_TICKS, POISON_AMPLIFIER),
                (Entity) (Object) this);
    }
}
