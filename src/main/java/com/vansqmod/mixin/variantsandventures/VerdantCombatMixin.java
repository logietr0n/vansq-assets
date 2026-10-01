package com.vansqmod.mixin.variantsandventures;

import com.vansqmod.compat.VerdantCombat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Verdants fire a 3-arrow volley, leap on the first shot, and deal 30% arrow damage.
 * Poison is left on the original {@code getArrow} implementation.
 */
@Mixin(targets = "com.faboslav.variantsandventures.common.entity.mob.VerdantEntity")
public abstract class VerdantCombatMixin {

    @Unique
    private int vansqmod$burstRemaining;
    @Unique
    private int vansqmod$burstDelay;
    @Unique
    private float vansqmod$burstPower;
    @Unique
    private boolean vansqmod$inBurstFollowUp;
    @Unique
    private int vansqmod$chaseJumpCooldown;

    @Inject(method = "tick", at = @At("TAIL"))
    private void vansqmod$tickBurst(CallbackInfo ci) {
        AbstractSkeleton verdant = (AbstractSkeleton) (Object) this;
        if (verdant.level().isClientSide || !verdant.isAlive()) {
            return;
        }
        if (this.vansqmod$chaseJumpCooldown > 0) {
            this.vansqmod$chaseJumpCooldown--;
        } else if (VerdantCombat.tryChaseJump(verdant)) {
            this.vansqmod$chaseJumpCooldown = VerdantCombat.CHASE_JUMP_COOLDOWN;
        }
        if (this.vansqmod$burstRemaining <= 0) {
            return;
        }
        if (--this.vansqmod$burstDelay > 0) {
            return;
        }

        LivingEntity target = verdant.getTarget();
        if (target == null || !target.isAlive() || !verdant.hasLineOfSight(target)) {
            this.vansqmod$burstRemaining = 0;
            return;
        }

        this.vansqmod$inBurstFollowUp = true;
        verdant.performRangedAttack(target, this.vansqmod$burstPower);
        this.vansqmod$inBurstFollowUp = false;
        this.vansqmod$burstRemaining--;
        this.vansqmod$burstDelay = VerdantCombat.BURST_INTERVAL_TICKS;
    }

    @Inject(method = "performRangedAttack", at = @At("HEAD"))
    private void vansqmod$jumpOnAttack(LivingEntity target, float power, CallbackInfo ci) {
        if (this.vansqmod$inBurstFollowUp) {
            return;
        }
        VerdantCombat.leapOnBowShot((AbstractSkeleton) (Object) this, target);
    }

    @Inject(method = "performRangedAttack", at = @At("TAIL"))
    private void vansqmod$startBurst(LivingEntity target, float power, CallbackInfo ci) {
        AbstractSkeleton verdant = (AbstractSkeleton) (Object) this;
        if (verdant.level().isClientSide || this.vansqmod$inBurstFollowUp) {
            return;
        }
        this.vansqmod$burstRemaining = VerdantCombat.BURST_EXTRA_SHOTS;
        this.vansqmod$burstDelay = VerdantCombat.BURST_INTERVAL_TICKS;
        this.vansqmod$burstPower = power;
    }

    @Inject(method = "getArrow", at = @At("RETURN"))
    private void vansqmod$scaleArrowDamage(
            ItemStack arrow,
            float velocity,
            ItemStack weapon,
            CallbackInfoReturnable<AbstractArrow> cir
    ) {
        AbstractArrow projectile = cir.getReturnValue();
        if (projectile != null) {
            projectile.setBaseDamage(projectile.getBaseDamage() * VerdantCombat.ARROW_DAMAGE_SCALE);
        }
    }
}
