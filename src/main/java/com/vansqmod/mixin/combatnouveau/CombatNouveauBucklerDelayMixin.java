package com.vansqmod.mixin.combatnouveau;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tallestred.piglinproliferation.common.items.BucklerItem;

/**
 * Combat Nouveau {@code remove_shield_delay} sets use duration to
 * {@code getUseDuration() - 5}. Bucklers only last 10 ticks, so that cuts
 * the charge windup in half and can stack with other Start-duration edits
 * down to 0 (use never starts).
 */
@Mixin(targets = "fuzs.combatnouveau.handler.CombatTestHandler", remap = false)
public abstract class CombatNouveauBucklerDelayMixin {

    @Redirect(
            method = "onUseItemStart",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;",
                    remap = true
            )
    )
    private static UseAnim vansqmod$bucklerKeepsFullWindup(ItemStack stack) {
        if (stack.getItem() instanceof BucklerItem) {
            return UseAnim.NONE;
        }
        return stack.getUseAnimation();
    }
}
