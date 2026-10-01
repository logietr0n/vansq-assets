package com.vansqmod.client;

import com.spydnel.backpacks.registry.BPItems;
import com.vansqmod.VansqMod;
import me.juancarloscp52.spyglass_improvements.client.SpyglassImprovementsClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import pepjebs.mapatlases.client.MapAtlasesClient;

import java.util.List;

@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class BackpackTooltipHandler {

    private BackpackTooltipHandler() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        if (stack.is(BPItems.BACKPACK)) {
            event.getToolTip().add(keybindLine(
                    "tooltip.vansqmod.backpack.place_pickup",
                    ModKeyMappings.PLACE_PICKUP_BACKPACK,
                    "R"
            ));
            return;
        }

        if (stack.is(Items.SPYGLASS) && ModList.get().isLoaded("spyglass_improvements")) {
            addSpyglassLine(event.getToolTip());
            return;
        }

        if (isAtlas(stack) && ModList.get().isLoaded("map_atlases")) {
            addAtlasLine(event.getToolTip());
        }
    }

    private static void addSpyglassLine(List<Component> tooltip) {
        insertAfterName(tooltip, keybindLine(
                "tooltip.vansqmod.spyglass.use",
                SpyglassImprovementsClient.useSpyglass,
                "?"
        ));
    }

    private static void addAtlasLine(List<Component> tooltip) {
        insertAfterName(tooltip, keybindLine(
                "tooltip.vansqmod.atlas.open_map",
                MapAtlasesClient.OPEN_ATLAS_KEYBIND,
                "?"
        ));
    }

    private static boolean isAtlas(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return "map_atlases".equals(id.getNamespace()) && id.getPath().contains("atlas");
    }

    private static void insertAfterName(List<Component> tooltip, Component line) {
        tooltip.add(Math.min(1, tooltip.size()), line);
    }

    private static Component keybindLine(String translationKey, KeyMapping mapping, String fallback) {
        Component keyName = mapping != null
                ? mapping.getTranslatedKeyMessage()
                : Component.literal(fallback);
        return Component.translatable(translationKey, keyName).withStyle(ChatFormatting.GRAY);
    }
}
