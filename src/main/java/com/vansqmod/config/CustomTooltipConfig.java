package com.vansqmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vansqmod.VansqMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Loads {@code config/vansqmod/vansq_tooltips.json}. Each item ID maps to one or more tooltip lines.
 * <p>
 * Lines can be a plain string, or an object with {@code text}, optional {@code color}, and optional
 * {@code italic} / {@code bold}. Uncolored lines are always light gray ({@link ChatFormatting#GRAY}).
 * Colors may be Minecraft names ({@code gray}, {@code red}, {@code gold}), hex ({@code #RRGGBB}), or
 * legacy section-sign codes inside {@code text} ({@code §7gray}).
 * <p>
 * Placement: set global {@code _placement} to {@code after_name} or {@code bottom}, or per-item
 * {@code "placement": "bottom"} with a {@code lines} array. Reload with {@code /vansq reloadconfig}.
 */
public final class CustomTooltipConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config/vansqmod/vansq_tooltips.json");

    private static volatile Map<ResourceLocation, CustomTooltipEntry> entriesByItem = Map.of();
    private static volatile TooltipPlacement defaultPlacement = TooltipPlacement.AFTER_NAME;

    private CustomTooltipConfig() {
    }

    public static @Nullable CustomTooltipEntry entryFor(ResourceLocation itemId) {
        return entriesByItem.get(itemId);
    }

    public static int entryCount() {
        return entriesByItem.size();
    }

    public static void load() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                writeDefaultConfig();
            }

            TooltipPlacement globalPlacement = TooltipPlacement.AFTER_NAME;
            Map<ResourceLocation, CustomTooltipEntry> parsed = new LinkedHashMap<>();
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                if (root.has("_placement")) {
                    globalPlacement = TooltipPlacement.parse(root.get("_placement").getAsString(), globalPlacement);
                }
                for (var entry : root.entrySet()) {
                    if (entry.getKey().startsWith("_")) {
                        continue;
                    }
                    ResourceLocation itemId = ResourceLocation.tryParse(entry.getKey());
                    if (itemId == null) {
                        VansqMod.LOGGER.warn("vansq_tooltips.json: skipping invalid item id '{}'", entry.getKey());
                        continue;
                    }
                    CustomTooltipEntry tooltipEntry = parseEntry(entry.getValue(), globalPlacement);
                    if (!tooltipEntry.isEmpty()) {
                        parsed.put(itemId, tooltipEntry);
                    }
                }
            }
            defaultPlacement = globalPlacement;
            entriesByItem = Collections.unmodifiableMap(parsed);
            VansqMod.LOGGER.info("Loaded {} custom tooltip entries from {}", parsed.size(), CONFIG_PATH);
        } catch (Exception e) {
            VansqMod.LOGGER.error("Failed to load custom tooltips from {}", CONFIG_PATH, e);
            entriesByItem = Map.of();
            defaultPlacement = TooltipPlacement.AFTER_NAME;
        }
    }

    private static void writeDefaultConfig() throws java.io.IOException {
        Files.createDirectories(CONFIG_PATH.getParent());
        JsonObject root = new JsonObject();
        root.addProperty(
                "_comment",
                "Map item IDs to lines. Use /vansq reloadconfig after editing. Placement: after_name (default) or bottom."
        );
        root.addProperty("_placement", "after_name");
        root.addProperty(
                "_colors",
                "Named colors: gray, dark_gray, red, gold, aqua, ...  Hex: #RRGGBB. Or section signs in text (e.g. \\u00A77)."
        );

        JsonArray afterNameExample = new JsonArray();
        afterNameExample.add("Lines under the item name (default placement).");
        JsonObject colored = new JsonObject();
        colored.addProperty("text", "Named color example.");
        colored.addProperty("color", "gold");
        afterNameExample.add(colored);
        root.add("minecraft:iron_sword", afterNameExample);

        JsonObject bottomExample = new JsonObject();
        bottomExample.addProperty("placement", "bottom");
        JsonArray bottomLines = new JsonArray();
        bottomLines.add("These lines appear at the end of the tooltip.");
        JsonObject hex = new JsonObject();
        hex.addProperty("text", "Hex color example.");
        hex.addProperty("color", "#55FF55");
        bottomLines.add(hex);
        bottomExample.add("lines", bottomLines);
        root.add("minecraft:diamond_sword", bottomExample);

        Files.writeString(CONFIG_PATH, GSON.toJson(root));
    }

    private static CustomTooltipEntry parseEntry(JsonElement element, TooltipPlacement fallbackPlacement) {
        if (element == null || element.isJsonNull()) {
            return new CustomTooltipEntry(List.of(), fallbackPlacement);
        }
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("lines")) {
                TooltipPlacement placement = obj.has("placement")
                        ? TooltipPlacement.parse(obj.get("placement").getAsString(), fallbackPlacement)
                        : fallbackPlacement;
                return new CustomTooltipEntry(parseLines(obj.get("lines")), placement);
            }
            if (obj.has("text")) {
                Component line = parseLineElement(element);
                return new CustomTooltipEntry(
                        line.getString().isEmpty() ? List.of() : List.of(line),
                        fallbackPlacement
                );
            }
        }
        return new CustomTooltipEntry(parseLines(element), fallbackPlacement);
    }

    private static List<Component> parseLines(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return List.of();
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            Component line = parseLine(element.getAsString(), null, false, false);
            return line.getString().isEmpty() ? List.of() : List.of(line);
        }
        if (element.isJsonArray()) {
            List<Component> lines = new ArrayList<>();
            for (JsonElement child : element.getAsJsonArray()) {
                Component line = parseLineElement(child);
                if (!line.getString().isEmpty()) {
                    lines.add(line);
                }
            }
            return lines;
        }
        Component line = parseLineElement(element);
        return line.getString().isEmpty() ? List.of() : List.of(line);
    }

    private static Component parseLineElement(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return parseLine(element.getAsString(), null, false, false);
        }
        if (!element.isJsonObject()) {
            return Component.empty();
        }
        JsonObject obj = element.getAsJsonObject();
        String text = obj.has("text") ? obj.get("text").getAsString() : "";
        String color = obj.has("color") ? obj.get("color").getAsString() : null;
        boolean italic = obj.has("italic") && obj.get("italic").getAsBoolean();
        boolean bold = obj.has("bold") && obj.get("bold").getAsBoolean();
        return parseLine(text, color, italic, bold);
    }

    /**
     * Fresh copy for the tooltip list. Uncolored text is forced to light gray so later
     * handlers cannot leave a shared component as white, and sibling-based drawers still
     * see an explicit color.
     */
    public static Component prepareTooltipLine(Component line) {
        return forceDefaultGray(line);
    }

    private static MutableComponent forceDefaultGray(Component component) {
        Style style = component.getStyle();
        if (style.getColor() == null) {
            style = style.applyFormat(ChatFormatting.GRAY);
        }
        MutableComponent out = MutableComponent.create(component.getContents()).setStyle(style);
        for (Component sibling : component.getSiblings()) {
            out.append(forceDefaultGray(sibling));
        }
        return out;
    }

    private static Component parseLine(String text, @Nullable String colorName, boolean italic, boolean bold) {
        // Light gray unless the line has a resolved assigned color. Invalid names stay gray.
        Style base = Style.EMPTY.applyFormat(ChatFormatting.GRAY);
        TextColor named = resolveColor(colorName);
        if (named != null) {
            base = base.withColor(named);
        }
        if (italic) {
            base = base.withItalic(true);
        }
        if (bold) {
            base = base.withBold(true);
        }
        MutableComponent inner = containsLegacyFormatting(text)
                ? parseLegacyText(text, base)
                : Component.literal(text).setStyle(base);
        // Empty parent + sibling: drawers that ignore root style still pick up the color.
        return Component.empty().append(inner);
    }

    private static boolean containsLegacyFormatting(String text) {
        for (int i = 0; i < text.length() - 1; i++) {
            char c = text.charAt(i);
            if ((c == '\u00A7' || c == '&') && ChatFormatting.getByCode(text.charAt(i + 1)) != null) {
                return true;
            }
        }
        return false;
    }

    private static MutableComponent parseLegacyText(String input, Style baseStyle) {
        MutableComponent result = Component.empty();
        StringBuilder segment = new StringBuilder();
        Style style = baseStyle;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if ((c == '\u00A7' || c == '&') && i + 1 < input.length()) {
                ChatFormatting fmt = ChatFormatting.getByCode(input.charAt(i + 1));
                if (fmt != null) {
                    if (!segment.isEmpty()) {
                        result.append(Component.literal(segment.toString()).setStyle(style));
                        segment.setLength(0);
                    }
                    style = applyFormatting(baseStyle, style, fmt);
                    i++;
                    continue;
                }
            }
            segment.append(c);
        }

        if (!segment.isEmpty()) {
            result.append(Component.literal(segment.toString()).setStyle(style));
        }
        return result;
    }

    private static Style applyFormatting(Style baseStyle, Style current, ChatFormatting fmt) {
        return switch (fmt) {
            case OBFUSCATED -> current.withObfuscated(true);
            case BOLD -> current.withBold(true);
            case STRIKETHROUGH -> current.withStrikethrough(true);
            case UNDERLINE -> current.withUnderlined(true);
            case ITALIC -> current.withItalic(true);
            case RESET -> baseStyle;
            default -> {
                if (fmt.isColor()) {
                    yield current.withColor(fmt);
                }
                yield current;
            }
        };
    }

    private static @Nullable TextColor resolveColor(@Nullable String colorName) {
        return ConfigTextColor.resolve(colorName);
    }
}
