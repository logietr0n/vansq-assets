package com.vansqmod.client;

import com.vansqmod.VansqMod;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;
import java.util.Locale;

/**
 * Backup strip for the Dungeons Delight cleaver throw-range line (see also
 * {@link com.vansqmod.mixin.client.ItemStackCleaverTooltipMixin}). Item name colors for custom rarities
 * are handled by {@link com.vansqmod.mixin.ItemStackHoverNameMixin}.
 */
@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class DungeonsDelightTooltipHandler {

    /** Lang key for the green {@code 1.5x Throw Range} line on cleavers. */
    private static final String THROW_RANGE_KEY = "dungeonsdelight.tooltip.attribute.range";

    private DungeonsDelightTooltipHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        List<Component> tip = event.getToolTip();
        for (int i = tip.size() - 1; i >= 1; i--) {
            if (shouldStripThrowRangeLine(tip.get(i))) {
                tip.remove(i);
            }
        }
    }

    private static boolean shouldStripThrowRangeLine(Component line) {
        if (containsTranslatableKey(line, THROW_RANGE_KEY)) {
            return true;
        }
        String visual = line.getString().trim().toLowerCase(Locale.ROOT);
        return visual.contains("throw range") && visual.matches(".*\\d+\\.?\\d*x.*");
    }

    private static boolean containsTranslatableKey(Component line, String key) {
        ComponentContents contents = line.getContents();
        if (contents instanceof TranslatableContents tc && key.equals(tc.getKey())) {
            return true;
        }
        for (Component sibling : line.getSiblings()) {
            if (containsTranslatableKey(sibling, key)) {
                return true;
            }
        }
        return false;
    }
}
