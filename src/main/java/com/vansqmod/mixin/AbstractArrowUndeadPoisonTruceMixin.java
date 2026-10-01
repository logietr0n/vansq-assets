package com.vansqmod.mixin;

import com.vansqmod.compat.PoisonSkeletonUndeadTruce;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Poison arrows from Bogged / Verdant apply their effect to undead without dealing
 * projectile damage, so HurtByTarget does not turn the undead on the shooter.
 */
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowUndeadPoisonTruceMixin {

    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void vansqmod$undeadIgnorePoisonArrow(EntityHitResult result, CallbackInfo ci) {
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        if (!PoisonSkeletonUndeadTruce.isFriendlyPoisonHit(arrow, result.getEntity())) {
            return;
        }

        LivingEntity victim = (LivingEntity) result.getEntity();
        PoisonSkeletonUndeadTruce.beginFriendlyHit();
        try {
            ((AbstractArrowDoPostHurtInvoker) arrow).vansqmod$invokeDoPostHurtEffects(victim);
            PoisonSkeletonUndeadTruce.forgetShooter(victim, arrow.getOwner());
            arrow.playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F / (arrow.getRandom().nextFloat() * 0.2F + 0.9F));
            arrow.discard();
        } finally {
            PoisonSkeletonUndeadTruce.endFriendlyHit();
        }
        ci.cancel();
    }
}
