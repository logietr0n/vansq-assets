package com.vansqmod.compat;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import tallestred.piglinproliferation.common.enchantments.PPEnchantments;
import tallestred.piglinproliferation.common.items.BucklerItem;
import tallestred.piglinproliferation.common.items.component.PPComponents;
import tallestred.piglinproliferation.configuration.PPConfig;

/**
 * Dual-wield buckler: per-hand dashes, a 1-tick movement impulse, dash-state
 * length one-third of a Buckler, and the same cooldown
 * ({@code PPConfig.COMMON.bucklerCooldown}).
 */
public class RoseGoldDoubucklerItem extends BucklerItem {

    public static final int DURABILITY = 383;

    public RoseGoldDoubucklerItem() {
        super(new Item.Properties()
                .component(PPComponents.BUCKLER_IS_READY, false)
                .component(PPComponents.BUCKLER_CHARGE_TICKS, 0)
                .durability(DURABILITY));
    }

    public static Item create() {
        return new RoseGoldDoubucklerItem();
    }

    public static int cooldownTicks() {
        return PPConfig.COMMON.bucklerCooldown.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (player.isInWaterOrBubble()) {
            return InteractionResultHolder.pass(stack);
        }
        if (DoubucklerHands.isBlocking(player)) {
            return InteractionResultHolder.pass(stack);
        }
        if (BucklerItem.isReady(stack) && BucklerItem.getChargeTicks(stack) > 0) {
            return InteractionResultHolder.pass(stack);
        }
        if (BucklerItem.isReady(stack)) {
            BucklerItem.setReady(stack, false);
            BucklerItem.setChargeTicks(stack, 0);
        }
        player.getCooldowns().removeCooldown(this);
        if (DoubucklerHands.isOnCooldown(player, InteractionHand.MAIN_HAND)
                && DoubucklerHands.isOnCooldown(player, InteractionHand.OFF_HAND)) {
            return InteractionResultHolder.pass(stack);
        }
        InteractionHand dash = DoubucklerHands.isOnCooldown(player, InteractionHand.MAIN_HAND)
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        DoubucklerHands.beginUse(player, dash);
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        BucklerItem.setReady(stack, true);
        BucklerItem.setChargeTicks(stack, level);
        applyChargeModifiers(stack, entity);
        BucklerDashes.impulse(entity, stack);
        if (entity instanceof Player player) {
            DoubucklerHands.hurtShared(player, entity);
            player.getCooldowns().removeCooldown(this);
            InteractionHand used = DoubucklerHands.isAnimating(player, InteractionHand.OFF_HAND)
                    ? InteractionHand.OFF_HAND
                    : InteractionHand.MAIN_HAND;
            DoubucklerHands.startCooldown(player, used);
        } else {
            stack.hurtAndBreak(1, entity, EquipmentSlot.MAINHAND);
        }
        entity.stopUsingItem();
        return stack;
    }

    private static void applyChargeModifiers(ItemStack stack, LivingEntity entity) {
        BucklerItem.CHARGE_JUMP_PREVENTION.get().resetTransientModifier(entity);
        BucklerItem.INCREASED_KNOCKBACK_RESISTANCE.get().resetTransientModifier(entity);
        int turning = stack.getEnchantmentLevel(
                PPEnchantments.getEnchant(PPEnchantments.TURNING, entity.registryAccess())
        );
        BucklerItem.TURNING_SPEED_REDUCTION
                .getWithSummand(BucklerItem.turningReduction(turning))
                .resetTransientModifier(entity);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return DURABILITY;
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot slot, LivingEntity entity) {
        return slot != EquipmentSlot.OFFHAND && super.canEquip(stack, slot, entity);
    }
}
