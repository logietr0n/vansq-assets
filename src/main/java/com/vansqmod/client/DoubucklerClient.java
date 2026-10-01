package com.vansqmod.client;

import com.vansqmod.VansqMod;
import com.vansqmod.compat.DoubucklerHands;
import com.vansqmod.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import tallestred.piglinproliferation.common.items.BucklerItem;
import tallestred.piglinproliferation.configuration.PPConfig;

public final class DoubucklerClient {

    private DoubucklerClient() {
    }

    /**
     * World hands always use the in-hand mesh. GUI uses that mesh only when
     * the stack is the local player's mainhand, real offhand, or visual copy.
     */
    public static boolean useInHandModel(ItemStack stack, ItemDisplayContext context) {
        if (!DoubucklerHands.isDoubuckler(stack) || context == null) {
            return false;
        }
        if (context.firstPerson()
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
            return true;
        }
        if (context != ItemDisplayContext.GUI) {
            return false;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        return stack == player.getMainHandItem()
                || stack == player.getInventory().offhand.getFirst()
                || DoubucklerHands.isClientOffhandDisplay(stack);
    }

    /**
     * Both dashes write {@code isReady} on the real mainhand stack. The blocking
     * mesh must follow the hand that is actually dashing, from the render
     * context, never that shared ready flag.
     */
    public static boolean showBlockingModel(ItemStack stack, ItemDisplayContext context) {
        if (!DoubucklerHands.isDoubuckler(stack)) {
            return false;
        }
        Player holder = DoubucklerHands.renderPlayer();
        if (holder == null) {
            holder = Minecraft.getInstance().player;
        }
        if (holder == null || !DoubucklerHands.holdingInMain(holder)) {
            return false;
        }
        InteractionHand hand = handForContext(holder, context);
        if (hand != null) {
            return DoubucklerHands.isAnimating(holder, hand);
        }
        if (DoubucklerHands.isClientOffhandDisplay(stack)
                || stack == holder.getInventory().offhand.getFirst()) {
            return DoubucklerHands.isAnimating(holder, InteractionHand.OFF_HAND);
        }
        if (stack == holder.getMainHandItem()) {
            return DoubucklerHands.isAnimating(holder, InteractionHand.MAIN_HAND);
        }
        return false;
    }

    private static InteractionHand handForContext(Player player, ItemDisplayContext context) {
        if (context == null) {
            return null;
        }
        boolean left = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        boolean right = context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        if (!left && !right) {
            return null;
        }
        boolean mainIsLeft = player.getMainArm() == HumanoidArm.LEFT;
        if (left) {
            return mainIsLeft ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        }
        return mainIsLeft ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    @EventBusSubscriber(modid = VansqMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            if (!ModList.get().isLoaded("piglinproliferation")) {
                return;
            }
            event.enqueueWork(() -> ItemProperties.register(
                    ModItems.ROSE_GOLD_DOUBUCKLER.get(),
                    ResourceLocation.parse("blocking"),
                    (stack, level, entity, seed) -> 0.0F
            ));
        }
    }

    @EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
    public static final class GameBus {
        @SubscribeEvent
        public static void onTooltip(ItemTooltipEvent event) {
            if (!ModList.get().isLoaded("piglinproliferation")) {
                return;
            }
            if (event.getItemStack().getItem() != ModItems.ROSE_GOLD_DOUBUCKLER.get()) {
                return;
            }
            if (event.getEntity() == null || !event.getEntity().isLocalPlayer()) {
                return;
            }
            if (!PPConfig.CLIENT.bucklerDesc.get()) {
                return;
            }
            if (!(event.getItemStack().getItem() instanceof BucklerItem buckler)) {
                return;
            }
            event.getToolTip().add(Component.empty());
            event.getToolTip().addAll(buckler.getDescription(event.getItemStack()));
        }
    }
}
