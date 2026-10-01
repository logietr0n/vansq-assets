package com.vansqmod.mixin;

import com.vansqmod.compat.BucklerDashes;
import com.vansqmod.compat.RoseGoldDoubucklerItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tallestred.piglinproliferation.common.items.BucklerItem;

/**
 * Piglin Proliferation blocks buckler use in rain via
 * {@code isInWaterRainOrBubble()}. The item text only mentions water; rain
 * made every outdoor charge fail.
 */
@Mixin(BucklerItem.class)
public abstract class BucklerItemUseMixin {

    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;isInWaterRainOrBubble()Z"
            )
    )
    private boolean vansqmod$blockChargeInWaterOnly(Player player) {
        return player.isInWaterOrBubble();
    }

    @Inject(method = "finishUsingItem", at = @At("RETURN"))
    private void vansqmod$bucklerImpulse(
            ItemStack stack,
            Level level,
            LivingEntity entity,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (stack.getItem() instanceof RoseGoldDoubucklerItem) {
            return;
        }
        BucklerDashes.impulse(entity, stack);
    }

    @Redirect(
            method = "moveFowards",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(DDD)V"
            )
    )
    private static void vansqmod$noRailDash(LivingEntity entity, double x, double y, double z) {
    }

    @Inject(method = "startingChargeTicks", at = @At("RETURN"), cancellable = true)
    private static void vansqmod$thirdDoubucklerDashState(
            ItemStack stack,
            Level level,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (stack.getItem() instanceof RoseGoldDoubucklerItem) {
            cir.setReturnValue(Math.max(1, Math.round(cir.getReturnValueI() / 3.0F)));
        }
    }

    @Redirect(
            method = "bucklerBash",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"
            )
    )
    private static void vansqmod$halfDoubucklerKnockback(
            LivingEntity target,
            double strength,
            double x,
            double z,
            LivingEntity attacker
    ) {
        if (attacker.getMainHandItem().getItem() instanceof RoseGoldDoubucklerItem) {
            strength *= 0.5D;
        }
        target.knockback(strength, x, z);
    }

    @Redirect(
            method = "bucklerBash",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            )
    )
    private static boolean vansqmod$halfDoubucklerDamage(
            LivingEntity target,
            DamageSource source,
            float amount,
            LivingEntity attacker
    ) {
        if (attacker.getMainHandItem().getItem() instanceof RoseGoldDoubucklerItem) {
            amount *= 0.5F;
        }
        return target.hurt(source, amount);
    }
}
