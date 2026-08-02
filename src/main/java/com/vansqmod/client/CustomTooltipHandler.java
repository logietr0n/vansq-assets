package com.vansqmod.client;

import com.vansqmod.VansqMod;
import com.vansqmod.config.CustomTooltipConfig;
import com.vansqmod.config.CustomTooltipEntry;
import com.vansqmod.config.TooltipPlacement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

/**
 * Appends lines from {@code config/vansqmod/vansq_tooltips.json} to matching item tooltips.
 */
@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class CustomTooltipHandler {

    private CustomTooltipHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) {
            return;
        }

        CustomTooltipEntry entry = CustomTooltipConfig.entryFor(id);
        if (entry == null || entry.isEmpty()) {
            return;
        }

        List<Component> tip = event.getToolTip();
        if (entry.placement() == TooltipPlacement.BOTTOM) {
            tip.addAll(entry.lines());
            return;
        }

        int insertAt = Math.min(1, tip.size());
        for (Component line : entry.lines()) {
            tip.add(insertAt++, line);
        }
    }
}
