package com.vansqmod.compat;

import com.vansqmod.registry.ModAttachments;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import tallestred.piglinproliferation.common.items.BucklerItem;

public final class DoubucklerHands {

    private static ItemStack clientOffhandDisplay = ItemStack.EMPTY;
    private static final ThreadLocal<Player> RENDER_PLAYER = new ThreadLocal<>();

    private DoubucklerHands() {
    }

    public static boolean isDoubuckler(ItemStack stack) {
        return stack.getItem() instanceof RoseGoldDoubucklerItem;
    }

    public static boolean holdingInMain(Player player) {
        return isDoubuckler(player.getMainHandItem());
    }

    public static DoubucklerState get(Player player) {
        return player.getData(ModAttachments.DOUBUCKLER_HANDS.get());
    }

    public static void set(Player player, DoubucklerState state) {
        player.setData(ModAttachments.DOUBUCKLER_HANDS.get(), state);
    }

    public static boolean isOnCooldown(Player player, InteractionHand hand) {
        return remaining(player, hand) > 0;
    }

    public static int remaining(Player player, InteractionHand hand) {
        return get(player).remaining(hand == InteractionHand.MAIN_HAND, player.level().getGameTime());
    }

    public static float cooldownPercent(Player player, InteractionHand hand, float partialTick) {
        int left = remaining(player, hand);
        int total = RoseGoldDoubucklerItem.cooldownTicks();
        if (left <= 0) {
            return 0.0F;
        }
        return Mth.clamp((left - partialTick) / total, 0.0F, 1.0F);
    }

    public static void invalidateOffhandDisplay() {
        clientOffhandDisplay = ItemStack.EMPTY;
    }

    public static boolean isClientOffhandDisplay(ItemStack stack) {
        return !stack.isEmpty() && stack == clientOffhandDisplay;
    }

    /**
     * One dash spends 1 durability on the shared pair. Both the real mainhand
     * stack and any offhand Doubuckler are set to the same remaining damage.
     */
    public static void hurtShared(Player player, LivingEntity entity) {
        ItemStack main = player.getMainHandItem();
        if (!isDoubuckler(main)) {
            return;
        }
        main.hurtAndBreak(1, entity, EquipmentSlot.MAINHAND);
        syncDurability(player);
        invalidateOffhandDisplay();
    }

    public static void syncDurability(Player player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getInventory().offhand.getFirst();
        if (!isDoubuckler(off)) {
            return;
        }
        if (!isDoubuckler(main) || main.isEmpty()) {
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            return;
        }
        off.setDamageValue(main.getDamageValue());
    }

    public static void startCooldown(Player player, InteractionHand hand) {
        int ticks = RoseGoldDoubucklerItem.cooldownTicks();
        long readyAt = player.level().getGameTime() + ticks;
        set(player, get(player).withReadyAt(hand == InteractionHand.MAIN_HAND, readyAt));
        player.getCooldowns().removeCooldown(player.getMainHandItem().getItem());
    }

    public static void beginUse(Player player, InteractionHand hand) {
        set(player, get(player).withAnim(
                hand == InteractionHand.OFF_HAND ? DoubucklerState.ANIM_OFF : DoubucklerState.ANIM_MAIN
        ));
    }

    public static boolean isAnimating(Player player, InteractionHand hand) {
        int anim = get(player).animHand();
        if (anim == DoubucklerState.ANIM_NONE) {
            return false;
        }
        return hand == InteractionHand.OFF_HAND
                ? anim == DoubucklerState.ANIM_OFF
                : anim == DoubucklerState.ANIM_MAIN;
    }

    public static InteractionHand animationHand(Player player) {
        int anim = get(player).animHand();
        if (anim == DoubucklerState.ANIM_OFF) {
            return InteractionHand.OFF_HAND;
        }
        if (anim == DoubucklerState.ANIM_MAIN) {
            return InteractionHand.MAIN_HAND;
        }
        return player.getUsedItemHand();
    }

    /**
     * True only during the block windup. A finished mainhand dash must not
     * lock the offhand; leftover anim / ready flags stay on the shared stack.
     */
    public static boolean isBlocking(Player player) {
        return player.isUsingItem() && isDoubuckler(player.getUseItem());
    }

    public static void pushRenderPlayer(Player player) {
        RENDER_PLAYER.set(player);
    }

    public static void popRenderPlayer() {
        RENDER_PLAYER.remove();
    }

    public static Player renderPlayer() {
        return RENDER_PLAYER.get();
    }

    public static ItemStack displayOffhand(Player player) {
        if (!holdingInMain(player)) {
            clientOffhandDisplay = ItemStack.EMPTY;
            return player.getInventory().offhand.getFirst();
        }
        ItemStack display = player.getMainHandItem().copy();
        boolean offDash = isAnimating(player, InteractionHand.OFF_HAND);
        BucklerItem.setReady(display, offDash);
        BucklerItem.setChargeTicks(display, offDash ? BucklerItem.getChargeTicks(player.getMainHandItem()) : 0);
        if (ItemStack.matches(clientOffhandDisplay, display)) {
            return clientOffhandDisplay;
        }
        clientOffhandDisplay = display;
        return display;
    }

    public static void tick(Player player) {
        ejectFromOffhand(player);
        if (holdingInMain(player)) {
            player.getCooldowns().removeCooldown(player.getMainHandItem().getItem());
            syncDurability(player);
        }
        clearAnimIfIdle(player);
    }

    private static void clearAnimIfIdle(Player player) {
        DoubucklerState state = get(player);
        if (state.animHand() == DoubucklerState.ANIM_NONE) {
            return;
        }
        ItemStack main = player.getMainHandItem();
        boolean using = player.isUsingItem() && isDoubuckler(player.getUseItem());
        boolean charging = isDoubuckler(main)
                && (BucklerItem.isReady(main) || BucklerItem.getChargeTicks(main) > 0);
        if (!using && !charging) {
            set(player, state.withAnim(DoubucklerState.ANIM_NONE));
        }
    }

    public static void ejectFromOffhand(Player player) {
        ItemStack off = player.getInventory().offhand.getFirst();
        if (!isDoubuckler(off)) {
            return;
        }
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        invalidateOffhandDisplay();
        if (!player.addItem(off)) {
            player.drop(off, false);
        }
    }
}
