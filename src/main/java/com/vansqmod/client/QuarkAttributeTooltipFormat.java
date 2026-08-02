package com.vansqmod.client;

import com.vansqmod.VansqMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.Set;

/**
 * Formatting helpers for Quark attribute tooltips.
 * Kept out of the mixin class so switch/lambda synthetics are never nestmates of a Mixin
 * (those can fail to load after Mixin inlines the handler into Quark, crashing FancyMenu wraps).
 */
public final class QuarkAttributeTooltipFormat {

    private static final Set<ResourceLocation> CC_PERCENT_ATTRIBUTES = Set.of(
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "magic_protection"),
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "experience_boost"),
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "lifesteal"),
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "stealth")
    );

    private QuarkAttributeTooltipFormat() {
    }

    /**
     * @return replacement tooltip text, or {@code null} to keep Quark's default formatting
     */
    public static MutableComponent tryFormat(ItemAttributeModifiers.Entry entry, double baseVal) {
        ResourceLocation modifierId = entry.modifier().id();

        if (entry.attribute().is(Attributes.MAX_HEALTH)
                && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE
                && isVansqSanguineHealthModifier(modifierId)) {
            return formatFlat(entry.modifier().amount());
        }

        if (entry.attribute().is(Attributes.ATTACK_DAMAGE)
                && isVansqArmorAttackDamageModifier(modifierId)) {
            AttributeModifier.Operation op = entry.modifier().operation();
            if (op == AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    || op == AttributeModifier.Operation.ADD_VALUE) {
                return formatSignedPercent(entry.modifier().amount());
            }
            return null;
        }

        ResourceLocation attributeId = entry.attribute().unwrapKey()
                .map(key -> key.location())
                .orElse(null);
        if (attributeId == null || !CC_PERCENT_ATTRIBUTES.contains(attributeId)) {
            return null;
        }

        AttributeModifier.Operation op = entry.modifier().operation();
        double percentValue;
        if (op == AttributeModifier.Operation.ADD_VALUE) {
            percentValue = entry.modifier().amount() + baseVal;
        } else if (op == AttributeModifier.Operation.ADD_MULTIPLIED_BASE) {
            percentValue = entry.modifier().amount();
        } else {
            return null;
        }

        return formatSignedPercent(percentValue);
    }

    private static boolean isVansqSanguineHealthModifier(ResourceLocation modifierId) {
        return VansqMod.MODID.equals(modifierId.getNamespace())
                && modifierId.getPath().startsWith("sanguine_health.");
    }

    private static boolean isVansqArmorAttackDamageModifier(ResourceLocation modifierId) {
        return VansqMod.MODID.equals(modifierId.getNamespace())
                && (modifierId.getPath().startsWith("chainmail_attack_damage.")
                || modifierId.getPath().startsWith("sanguine_attack_damage."));
    }

    private static MutableComponent formatFlat(double value) {
        return Component.literal(ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(value))
                .withStyle(value < 0 ? ChatFormatting.RED : ChatFormatting.WHITE);
    }

    private static MutableComponent formatSignedPercent(double decimalValue) {
        return Component.literal(
                        (decimalValue > 0 ? "+" : "")
                                + ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(decimalValue * 100.0D)
                                + "%")
                .withStyle(decimalValue < 0 ? ChatFormatting.RED : ChatFormatting.WHITE);
    }
}
