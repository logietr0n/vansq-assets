package com.vansqmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vansqmod.VansqMod;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Loads {@code config/vansqmod/vansq_rarities.json}. Overrides the hover (item name) color per item ID
 * using a vanilla {@link Rarity} tier or a hex / named color.
 */
public final class ItemNameColorConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config/vansqmod/vansq_rarities.json");

    private static volatile Map<ResourceLocation, ItemNameStyleOverride> overridesByItem = Map.of();

    private ItemNameColorConfig() {
    }

    public static @Nullable ItemNameStyleOverride overrideFor(ResourceLocation itemId) {
        return overridesByItem.get(itemId);
    }

    public static int entryCount() {
        return overridesByItem.size();
    }

    public static void load() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                writeDefaultConfig();
            }

            Map<ResourceLocation, ItemNameStyleOverride> parsed = new LinkedHashMap<>();
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                for (var entry : root.entrySet()) {
                    if (entry.getKey().startsWith("_")) {
                        continue;
                    }
                    ResourceLocation itemId = ResourceLocation.tryParse(entry.getKey());
                    if (itemId == null) {
                        VansqMod.LOGGER.warn("vansq_rarities.json: skipping invalid item id '{}'", entry.getKey());
                        continue;
                    }
                    ItemNameStyleOverride override = parseOverride(entry.getValue());
                    if (override != null) {
                        parsed.put(itemId, override);
                    }
                }
            }
            overridesByItem = Collections.unmodifiableMap(parsed);
            VansqMod.LOGGER.info("Loaded {} item name color overrides from {}", parsed.size(), CONFIG_PATH);
        } catch (Exception e) {
            VansqMod.LOGGER.error("Failed to load item name colors from {}", CONFIG_PATH, e);
            overridesByItem = Map.of();
        }
    }

    private static void writeDefaultConfig() throws java.io.IOException {
        Files.createDirectories(CONFIG_PATH.getParent());
        JsonObject root = new JsonObject();
        root.addProperty(
                "_comment",
                "Override item name colors. Use /vansq reloadconfig after editing."
        );
        root.addProperty(
                "_values",
                "String shorthand: common | uncommon | rare | epic | #RRGGBB | gold | aqua | ... Object: { \"rarity\": \"rare\" } or { \"color\": \"#FF5555\" }"
        );
        root.addProperty("minecraft:iron_sword", "uncommon");
        root.addProperty("minecraft:netherite_ingot", "#8060C0");
        JsonObject named = new JsonObject();
        named.addProperty("color", "gold");
        root.add("minecraft:golden_apple", named);
        Files.writeString(CONFIG_PATH, GSON.toJson(root));
    }

    private static @Nullable ItemNameStyleOverride parseOverride(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return parseShorthand(element.getAsString());
        }
        if (!element.isJsonObject()) {
            return null;
        }
        JsonObject obj = element.getAsJsonObject();
        if (obj.has("rarity")) {
            Rarity rarity = parseRarity(obj.get("rarity").getAsString());
            return rarity != null ? ItemNameStyleOverride.fromRarity(rarity) : null;
        }
        if (obj.has("color")) {
            TextColor color = ConfigTextColor.resolve(obj.get("color").getAsString());
            return color != null ? ItemNameStyleOverride.fromColor(color) : null;
        }
        return null;
    }

    private static @Nullable ItemNameStyleOverride parseShorthand(String raw) {
        String value = raw.trim();
        if (value.isEmpty()) {
            return null;
        }
        Rarity rarity = parseRarity(value);
        if (rarity != null) {
            return ItemNameStyleOverride.fromRarity(rarity);
        }
        TextColor color = ConfigTextColor.resolve(value);
        return color != null ? ItemNameStyleOverride.fromColor(color) : null;
    }

    private static @Nullable Rarity parseRarity(String name) {
        return switch (name.trim().toLowerCase(Locale.ROOT)) {
            case "common", "white" -> Rarity.COMMON;
            case "uncommon", "yellow" -> Rarity.UNCOMMON;
            case "rare", "aqua", "cyan" -> Rarity.RARE;
            case "epic", "light_purple", "lightpurple", "magenta" -> Rarity.EPIC;
            default -> null;
        };
    }
}
