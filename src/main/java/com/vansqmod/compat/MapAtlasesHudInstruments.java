package com.vansqmod.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Predicate;

/**
 * Held / Curios checks for Map Atlases HUD coordinate / time readout gates.
 * Compass, clock, depth gauge, and atlas count when held or equipped in Curios
 * (including the Atlas hands slot).
 */
public final class MapAtlasesHudInstruments {

    private static final ResourceLocation ATLAS_ID =
            ResourceLocation.fromNamespaceAndPath("map_atlases", "atlas");
    private static final ResourceLocation DEPTH_GAUGE_ID =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "depth_gauge");
    private static final ResourceLocation ALTIMETER_ID =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "altimeter");

    private MapAtlasesHudInstruments() {
    }

    public static boolean hasAtlas(Player player) {
        return isEquipped(player, MapAtlasesHudInstruments::isAtlas);
    }

    /** Compass unlocks horizontal coordinates (world X/Z). */
    public static boolean hasCompass(Player player) {
        return isEquippedItem(player, Items.COMPASS) || isEquippedItem(player, Items.RECOVERY_COMPASS);
    }

    /** Depth gauge / altimeter unlocks altitude (world Y). */
    public static boolean hasAltimeter(Player player) {
        return isEquippedId(player, DEPTH_GAUGE_ID) || isEquippedId(player, ALTIMETER_ID);
    }

    public static boolean hasClock(Player player) {
        return isEquippedItem(player, Items.CLOCK);
    }

    public static boolean showHorizontalCoords(Player player) {
        return hasAtlas(player) || hasCompass(player);
    }

    public static boolean showAltitude(Player player) {
        return hasAtlas(player) || hasAltimeter(player);
    }

    public static boolean showTime(Player player) {
        return hasAtlas(player) || hasClock(player);
    }

    public static boolean showAny(Player player) {
        return showHorizontalCoords(player) || showAltitude(player) || showTime(player);
    }

    /**
     * Formats world time like {@code 10:45 PM, Day 9}.
     * Minecraft day-time 0 is 06:00.
     */
    public static String formatDayAndTime(Level level) {
        long dayTime = level.getDayTime();
        long day = dayTime / 24000L;
        long timeOfDay = dayTime % 24000L;

        double hoursF = (timeOfDay / 1000.0D) + 6.0D;
        if (hoursF >= 24.0D) {
            hoursF -= 24.0D;
        }
        int hour24 = (int) hoursF;
        int minute = (int) ((hoursF - hour24) * 60.0D);

        boolean pm = hour24 >= 12;
        int hour12 = hour24 % 12;
        if (hour12 == 0) {
            hour12 = 12;
        }
        return hour12 + ":" + String.format("%02d", minute) + (pm ? " PM" : " AM") + ", Day " + day;
    }

    private static boolean isAtlas(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return ATLAS_ID.equals(id)
                || ("map_atlases".equals(id.getNamespace()) && id.getPath().startsWith("atlas"));
    }

    private static boolean isEquippedItem(Player player, Item item) {
        return isEquipped(player, stack -> stack.is(item));
    }

    private static boolean isEquippedId(Player player, ResourceLocation id) {
        return BuiltInRegistries.ITEM.getOptional(id)
                .map(item -> isEquippedItem(player, item))
                .orElse(false);
    }

    private static boolean isEquipped(Player player, Predicate<ItemStack> predicate) {
        return isHolding(player, predicate) || hasInCurios(player, predicate);
    }

    private static boolean isHolding(Player player, Predicate<ItemStack> predicate) {
        ItemStack main = player.getMainHandItem();
        if (!main.isEmpty() && predicate.test(main)) {
            return true;
        }
        ItemStack off = player.getOffhandItem();
        return !off.isEmpty() && predicate.test(off);
    }

    private static boolean hasInCurios(Player player, Predicate<ItemStack> predicate) {
        if (!ModList.get().isLoaded("curios")) {
            return false;
        }
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findFirstCurio(predicate))
                .isPresent();
    }
}
