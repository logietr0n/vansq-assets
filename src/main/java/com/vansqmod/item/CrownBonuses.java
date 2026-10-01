package com.vansqmod.item;

import com.google.common.collect.Multimap;
import com.vansqmod.VansqMod;
import com.vansqmod.registry.ModAttributes;
import com.vansqmod.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * Crown stats. Experience boost and magic protection are item attribute modifiers so Quark
 * can icon them. Fortune, looting, and magic-damage conversion are left off that list;
 * those lines come from {@code vansq_tooltips.json}. Worn modifiers still apply the
 * effects Curios does not copy from a non-armor head slot.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class CrownBonuses {

    public static final String HEAD_SLOT = "head";

    public static final double FORTUNE = 1.0D;
    public static final double LOOTING = 1.0D;
    /** Caverns &amp; Chasms experience boost. 0.20 displays as +20%. */
    public static final double EXPERIENCE_BOOST = 0.20D;
    /**
     * Caverns &amp; Chasms magic protection is a fraction (a silver armor piece is 0.15, shown as +15%).
     * 0.40 displays as +40%.
     */
    public static final double MAGIC_PROTECTION = 0.40D;
    /** Share of dealt attack damage turned into magic damage. Tooltip text is in vansq_tooltips.json. */
    public static final double MAGIC_CONVERSION = 0.20D;

    private static final ResourceLocation EXPERIENCE_BOOST_ID =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "experience_boost");
    private static final ResourceLocation MAGIC_PROTECTION_ID =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "magic_protection");

    private CrownBonuses() {
    }

    @SubscribeEvent
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.is(ModItems.GOLDEN_CROWN.get())) {
            addRegistered(event, EXPERIENCE_BOOST_ID, EXPERIENCE_BOOST, "golden_crown.experience");
        } else if (stack.is(ModItems.SILVER_CROWN.get())) {
            addRegistered(event, MAGIC_PROTECTION_ID, MAGIC_PROTECTION, "silver_crown.magic_protection");
        }
    }

    public static void addWornModifiers(Multimap<Holder<Attribute>, AttributeModifier> modifiers, ItemStack stack) {
        if (stack.is(ModItems.GOLDEN_CROWN.get())) {
            putRegistered(modifiers, EXPERIENCE_BOOST_ID, EXPERIENCE_BOOST, "golden_crown.experience");
        } else if (stack.is(ModItems.SILVER_CROWN.get())) {
            putRegistered(modifiers, MAGIC_PROTECTION_ID, MAGIC_PROTECTION, "silver_crown.magic_protection");
            put(modifiers, ModAttributes.MAGIC_CONVERSION, MAGIC_CONVERSION, "silver_crown.magic_conversion");
        }
    }

    private static void addRegistered(
            ItemAttributeModifierEvent event,
            ResourceLocation attributeId,
            double amount,
            String key
    ) {
        BuiltInRegistries.ATTRIBUTE.getHolder(attributeId).ifPresent(attribute ->
                event.replaceModifier(attribute, modifier(key, amount), EquipmentSlotGroup.HEAD));
    }

    private static void put(
            Multimap<Holder<Attribute>, AttributeModifier> modifiers,
            Holder<Attribute> attribute,
            double amount,
            String key
    ) {
        modifiers.put(attribute, modifier(key, amount));
    }

    private static void putRegistered(
            Multimap<Holder<Attribute>, AttributeModifier> modifiers,
            ResourceLocation attributeId,
            double amount,
            String key
    ) {
        BuiltInRegistries.ATTRIBUTE.getHolder(attributeId).ifPresent(attribute ->
                modifiers.put(attribute, modifier(key, amount)));
    }

    private static AttributeModifier modifier(String key, double amount) {
        return new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, key),
                amount,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
}
