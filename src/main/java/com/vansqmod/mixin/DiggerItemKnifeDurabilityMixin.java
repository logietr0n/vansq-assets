package com.vansqmod.mixin;

import com.vansqmod.compat.KnifeCombatDurability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla diggers take 2 durability as weapons. Knives tagged {@code #c:tools/knife}
 * instead use the intended-use cost, with only a 25% chance to lose 1 durability.
 */
@Mixin(DiggerItem.class)
public class DiggerItemKnifeDurabilityMixin {

    @Inject(method = "postHurtEnemy", at = @At("HEAD"), cancellable = true)
    private void vansqmod$knifeCombatDurability(
            ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        if (KnifeCombatDurability.handlePostHurtEnemy(stack, attacker)) {
            ci.cancel();
        }
    }
}
