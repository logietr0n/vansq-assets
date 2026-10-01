package com.vansqmod.client;

import com.vansqmod.VansqMod;
import com.vansqmod.integration.charm.CharmTotemEquipment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Set;

/**
 * Drops The Beyond Totem of Respite hardcoded description so vansq_tooltips.json can replace it.
 */
@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class TheBeyondTooltipHandler {

    private static final Set<String> RESPITE_DESCRIPTION_LINES = Set.of(
            "When in Hand:",
            "Preserve items on Death"
    );

    private TheBeyondTooltipHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !CharmTotemEquipment.isRespite(stack)) {
            return;
        }

        List<Component> tip = event.getToolTip();
        for (int i = tip.size() - 1; i >= 1; i--) {
            if (RESPITE_DESCRIPTION_LINES.contains(tip.get(i).getString().trim())) {
                tip.remove(i);
            }
        }
        for (int i = tip.size() - 1; i > 0; i--) {
            if (tip.get(i).getString().isBlank() && tip.get(i - 1).getString().isBlank()) {
                tip.remove(i);
            }
        }
    }
}
