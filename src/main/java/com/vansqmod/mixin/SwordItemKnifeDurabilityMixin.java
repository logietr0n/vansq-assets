package com.vansqmod.mixin;

import com.vansqmod.compat.KnifeCombatDurability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sword-backed knives (copper, rose gold) inherit sword combat wear of 1.
 * Roll the same 25% knife combat durability chance.
 */
@Mixin(SwordItem.class)
public class SwordItemKnifeDurabilityMixin {

    @Inject(method = "postHurtEnemy", at = @At("HEAD"), cancellable = true)
    private void vansqmod$knifeCombatDurability(
            ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        if (KnifeCombatDurability.handlePostHurtEnemy(stack, attacker)) {
            ci.cancel();
        }
    }
}
