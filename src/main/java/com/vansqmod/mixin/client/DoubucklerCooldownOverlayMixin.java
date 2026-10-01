package com.vansqmod.mixin.client;

import com.vansqmod.compat.DoubucklerHands;
import com.vansqmod.compat.RoseGoldDoubucklerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiGraphics.class)
public abstract class DoubucklerCooldownOverlayMixin {

    @Redirect(
            method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemCooldowns;getCooldownPercent(Lnet/minecraft/world/item/Item;F)F"
            )
    )
    private float vansqmod$perHandCooldown(
            ItemCooldowns cooldowns,
            Item item,
            float partialTick,
            Font font,
            ItemStack stack,
            int x,
            int y,
            String count
    ) {
        if (item instanceof RoseGoldDoubucklerItem) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                InteractionHand hand = stack == player.getMainHandItem()
                        ? InteractionHand.MAIN_HAND
                        : InteractionHand.OFF_HAND;
                return DoubucklerHands.cooldownPercent(player, hand, partialTick);
            }
        }
        return cooldowns.getCooldownPercent(item, partialTick);
    }
}
