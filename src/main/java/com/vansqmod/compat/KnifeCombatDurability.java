package com.vansqmod.compat;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Knives take combat durability like a tool's intended use (1 instead of 2),
 * then only 25% of those hits actually consume that point.
 */
public final class KnifeCombatDurability {

    private static final float COMBAT_BREAK_CHANCE = 0.25F;

    private KnifeCombatDurability() {
    }

    /**
     * @return {@code true} if this stack is a knife and combat durability was handled
     */
    public static boolean handlePostHurtEnemy(ItemStack stack, LivingEntity attacker) {
        if (stack.isEmpty() || !stack.is(KnifeNoKnockbackHandler.TOOLS_KNIFE)) {
            return false;
        }
        if (attacker.getRandom().nextFloat() < COMBAT_BREAK_CHANCE) {
            stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        }
        return true;
    }
}
