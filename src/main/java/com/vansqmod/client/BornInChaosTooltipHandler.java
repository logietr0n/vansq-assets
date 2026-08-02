package com.vansqmod.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.vansqmod.VansqMod;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Removes Born in Chaos custom tooltip <em>description</em> lines (keys like
 * {@code item.born_in_chaos_v1.&lt;id&gt;.description_0}), the {@code item.borninchaos.tooltip} shift hint,
 * and procedure literals that match those lang values. Other BiC tooltip content is left alone.
 */
@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class BornInChaosTooltipHandler {

    private static final String BORN_IN_CHAOS = "born_in_chaos_v1";

    /** BiC items that should keep their custom description / shift-hint tooltips. */
    private static final ResourceLocation DARK_UPGRADE =
            ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS, "dark_upgrade");

    /** Lang key for the “press Shift for more info” line (see {@code born_in_chaos_v1} {@code en_us.json}). */
    private static final String SHIFT_HINT_KEY = "item.borninchaos.tooltip";

    /**
     * Mirrors keys in {@code assets/born_in_chaos_v1/lang/*.json}:
     * {@code item.born_in_chaos_v1.dark_metal_armor_helmet.description_0} (not {@code gamerule.*.description}).
     */
    private static final Pattern DESCRIPTION_KEY = Pattern.compile(
            "^(?:item|block)\\." + Pattern.quote(BORN_IN_CHAOS) + "\\.[\\w]+\\.description_\\d+$"
    );

    private static volatile Set<String> cachedDescriptionTexts = null;

    private BornInChaosTooltipHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !isBornInChaos(stack) || isExcludedFromStripping(stack)) {
            return;
        }

        List<Component> tip = event.getToolTip();
        for (int i = tip.size() - 1; i >= 1; i--) {
            if (shouldStripDescriptionLine(tip.get(i), i)) {
                tip.remove(i);
            }
        }
        collapseConsecutiveBlankLines(tip);
    }

    /** After stripping description lines, BiC often leaves stacked empty rows; vanilla uses a single gap after the name. */
    private static void collapseConsecutiveBlankLines(List<Component> tip) {
        for (int i = tip.size() - 1; i > 0; i--) {
            if (isBlankLine(tip.get(i)) && isBlankLine(tip.get(i - 1))) {
                tip.remove(i);
            }
        }
    }

    private static boolean isBlankLine(Component line) {
        return line.getString().isBlank();
    }

    private static boolean isBornInChaos(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && BORN_IN_CHAOS.equals(id.getNamespace());
    }

    private static boolean isExcludedFromStripping(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return DARK_UPGRADE.equals(id);
    }

    private static boolean shouldStripDescriptionLine(Component line, int index) {
        if (index == 0) {
            return false;
        }
        if (containsStrippableTranslatable(line)) {
            return true;
        }
        return matchesKnownDescriptionLiteral(line);
    }

    private static boolean containsStrippableTranslatable(Component line) {
        if (line.getContents() instanceof TranslatableContents tc && isStrippableTranslatableKey(tc.getKey())) {
            return true;
        }
        for (Component sibling : line.getSiblings()) {
            if (containsStrippableTranslatable(sibling)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isStrippableTranslatableKey(String key) {
        return isDescriptionKey(key) || SHIFT_HINT_KEY.equals(key);
    }

    private static boolean isDescriptionKey(String key) {
        return DESCRIPTION_KEY.matcher(key).matches();
    }

    private static boolean matchesKnownDescriptionLiteral(Component line) {
        Set<String> texts = descriptionTexts();
        if (texts.isEmpty()) {
            return false;
        }
        String visual = line.getString().trim();
        return !visual.isEmpty() && texts.contains(visual);
    }

    private static Set<String> descriptionTexts() {
        Set<String> cached = cachedDescriptionTexts;
        if (cached != null) {
            return cached;
        }
        synchronized (BornInChaosTooltipHandler.class) {
            if (cachedDescriptionTexts != null) {
                return cachedDescriptionTexts;
            }
            Set<String> texts = loadDescriptionTextsFromLang();
            cachedDescriptionTexts = texts;
            return cachedDescriptionTexts;
        }
    }

    private static Set<String> loadDescriptionTextsFromLang() {
        Set<String> texts = new HashSet<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return Set.of();
        }
        @Nullable JsonObject root = readBornInChaosLang(mc);
        if (root == null) {
            return Set.of();
        }
        for (var e : root.entrySet()) {
            String key = e.getKey();
            if (!isStrippableTranslatableKey(key) || !e.getValue().isJsonPrimitive()) {
                continue;
            }
            String value = e.getValue().getAsString().trim();
            if (!value.isEmpty()) {
                texts.add(value);
            }
        }
        return Set.copyOf(texts);
    }

    private static @Nullable JsonObject readBornInChaosLang(Minecraft mc) {
        JsonObject merged = new JsonObject();
        mergeLangFile(mc, ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS, "lang/en_us.json"), merged);
        String selected = mc.getLanguageManager().getSelected();
        if (!"en_us".equals(selected)) {
            mergeLangFile(
                    mc,
                    ResourceLocation.fromNamespaceAndPath(BORN_IN_CHAOS, "lang/" + selected + ".json"),
                    merged
            );
        }
        return merged.isEmpty() ? null : merged;
    }

    private static void mergeLangFile(Minecraft mc, ResourceLocation rl, JsonObject into) {
        Optional<Resource> opt = mc.getResourceManager().getResource(rl);
        if (opt.isEmpty()) {
            return;
        }
        try (Reader reader = opt.get().openAsReader()) {
            JsonObject fragment = JsonParser.parseReader(reader).getAsJsonObject();
            for (var e : fragment.entrySet()) {
                into.add(e.getKey(), e.getValue());
            }
        } catch (Exception ignored) {
        }
    }
}
