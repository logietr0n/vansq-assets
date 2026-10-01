package com.vansqmod.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vansqmod.VansqMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Mirrors Item Obliterator's {@code config/item_obliterator.json5} blacklist so disabled items
 * cannot generate as loot, drop in the world, or appear on mobs. Reload with
 * {@code /vansq reloadconfig}.
 */
public final class ObliteratorItemConfig {

    private static final Path CONFIG_PATH = Path.of("config/item_obliterator.json5");

    private static volatile Set<String> exactIds = Set.of();
    private static volatile List<Pattern> regexes = List.of();
    private static volatile boolean loaded;

    private ObliteratorItemConfig() {
    }

    public static boolean isBlocked(@Nullable ItemStack stack) {
        return stack != null && !stack.isEmpty() && isBlocked(stack.getItem());
    }

    public static boolean isBlocked(@Nullable Item item) {
        if (item == null || item == Items.AIR) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return id != null && isBlocked(id.toString());
    }

    public static boolean isBlocked(@Nullable String itemId) {
        if (itemId == null || itemId.isEmpty() || "minecraft:air".equals(itemId)) {
            return false;
        }
        if (exactIds.contains(itemId)) {
            return true;
        }
        for (Pattern pattern : regexes) {
            if (pattern.matcher(itemId).matches()) {
                return true;
            }
        }
        return false;
    }

    public static int exactCount() {
        return exactIds.size();
    }

    public static int regexCount() {
        return regexes.size();
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static void load() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                VansqMod.LOGGER.warn("Item Obliterator config not found at {}, loot/equipment filter is idle", CONFIG_PATH);
                exactIds = Set.of();
                regexes = List.of();
                loaded = false;
                return;
            }
            String json = stripJson5(Files.readString(CONFIG_PATH));
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            boolean hashmap = root.has("use_hashmap_optimizations")
                    && root.get("use_hashmap_optimizations").getAsBoolean();
            JsonArray list = root.getAsJsonArray("blacklisted_items");
            Set<String> parsedExact = new HashSet<>();
            List<Pattern> parsedRegex = new ArrayList<>();
            if (list != null) {
                for (JsonElement element : list) {
                    if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                        continue;
                    }
                    String raw = element.getAsString();
                    if (raw == null || raw.isEmpty() || raw.startsWith("//")) {
                        continue;
                    }
                    if (!hashmap && raw.startsWith("!")) {
                        String regex = raw.substring(1);
                        try {
                            parsedRegex.add(Pattern.compile(regex));
                        } catch (PatternSyntaxException e) {
                            VansqMod.LOGGER.warn("item_obliterator.json5: skipping invalid regex '{}'", regex);
                        }
                    } else {
                        parsedExact.add(raw);
                    }
                }
            }
            exactIds = Set.copyOf(parsedExact);
            regexes = List.copyOf(parsedRegex);
            loaded = true;
            VansqMod.LOGGER.info(
                    "Loaded Item Obliterator blacklist ({} exact, {} regex) from {}",
                    exactIds.size(),
                    regexes.size(),
                    CONFIG_PATH
            );
        } catch (IOException | RuntimeException e) {
            VansqMod.LOGGER.error("Failed to load {}", CONFIG_PATH, e);
            exactIds = Set.of();
            regexes = List.of();
            loaded = false;
        }
    }

    /**
     * Drops {@code //} line comments and trailing commas so Gson can parse JSON5.
     */
    static String stripJson5(String json5) {
        StringBuilder out = new StringBuilder(json5.length());
        boolean inString = false;
        boolean escape = false;
        for (int i = 0; i < json5.length(); i++) {
            char c = json5.charAt(i);
            if (inString) {
                out.append(c);
                if (escape) {
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
                out.append(c);
                continue;
            }
            if (c == '/' && i + 1 < json5.length() && json5.charAt(i + 1) == '/') {
                i++;
                while (i + 1 < json5.length() && json5.charAt(i + 1) != '\n' && json5.charAt(i + 1) != '\r') {
                    i++;
                }
                continue;
            }
            out.append(c);
        }
        return out.toString().replaceAll(",(\\s*[}\\]])", "$1");
    }
}
