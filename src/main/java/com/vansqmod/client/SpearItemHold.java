package com.vansqmod.client;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import zzik2.barched.Barched;
import zzik2.barched.minecraft.world.item.SpearItem;

/**
 * Barched spears plus the {@code minecraft:spears} tag (vanilla iron/copper and
 * vansq rose gold / silver / necromium).
 */
public final class SpearItemHold {

    private SpearItemHold() {
    }

    public static ItemStack inArm(LivingEntity entity, HumanoidArm arm) {
        return entity.getItemInHand(
                entity.getMainArm() == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND
        );
    }

    public static boolean isSpear(ItemStack stack) {
        return !stack.isEmpty()
                && (stack.getItem() instanceof SpearItem || stack.is(Barched.ItemTags.SPEARS));
    }

    public static boolean isSpearInArm(LivingEntity entity, HumanoidArm arm) {
        return isSpear(inArm(entity, arm));
    }
}
