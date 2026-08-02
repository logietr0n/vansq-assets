package com.vansqmod.config;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

/**
 * @param styleModifier applied to hover-name text
 * @param rarity        vanilla rarity to expose via {@link net.minecraft.world.item.ItemStack#getRarity()},
 *                      or {@code null} for hex / named-color-only overrides
 */
public record ItemNameStyleOverride(UnaryOperator<Style> styleModifier, @Nullable Rarity rarity) {

    public static ItemNameStyleOverride fromRarity(Rarity rarity) {
        return new ItemNameStyleOverride(rarity.getStyleModifier(), rarity);
    }

    public static ItemNameStyleOverride fromColor(TextColor color) {
        return new ItemNameStyleOverride(style -> style.withColor(color), null);
    }
}
