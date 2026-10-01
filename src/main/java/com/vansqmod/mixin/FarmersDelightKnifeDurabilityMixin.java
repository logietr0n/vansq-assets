package com.vansqmod.mixin;

import com.vansqmod.compat.KnifeCombatDurability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Farmer's Delight {@code KnifeItem} overrides {@code postHurtEnemy}, so the
 * {@link DiggerItemKnifeDurabilityMixin} does not apply to those stacks.
 */
@Mixin(targets = "vectorwing.farmersdelight.common.item.KnifeItem")
public class FarmersDelightKnifeDurabilityMixin {

    @Inject(method = "postHurtEnemy", at = @At("HEAD"), cancellable = true)
    private void vansqmod$knifeCombatDurability(
            ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        if (KnifeCombatDurability.handlePostHurtEnemy(stack, attacker)) {
            ci.cancel();
        }
    }
}
