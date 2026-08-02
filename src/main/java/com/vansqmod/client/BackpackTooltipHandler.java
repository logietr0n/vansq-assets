package com.vansqmod.client;

import com.spydnel.backpacks.registry.BPItems;
import com.vansqmod.VansqMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class BackpackTooltipHandler {

    private BackpackTooltipHandler() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !stack.is(BPItems.BACKPACK)) {
            return;
        }

        Component keyName = ModKeyMappings.PLACE_PICKUP_BACKPACK != null
                ? ModKeyMappings.PLACE_PICKUP_BACKPACK.getTranslatedKeyMessage()
                : Component.literal("R");

        event.getToolTip().add(Component.translatable(
                "tooltip.vansqmod.backpack.place_pickup",
                keyName
        ).withStyle(ChatFormatting.GRAY));
    }
}
