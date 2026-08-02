package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * Compat: adjusts Abnormals Delight's silver knife to match this pack's knife feel
 * and to contribute a small, controlled amount of Caverns & Chasms magic damage.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class AbnormalsDelightSilverKnifeCompat {

    private static final ResourceLocation ABNORMALS_DELIGHT_SILVER_KNIFE =
            ResourceLocation.fromNamespaceAndPath("abnormals_delight", "silver_knife");

    // Match the knife attack speed pattern used elsewhere in this mod.
    // Vanilla base attack speed is 4.0; a +6.0 modifier yields 10.0 final.
    private static final double DESIRED_ATTACK_SPEED_MODIFIER = 6.0D;
    private static final ResourceLocation CAVERN_AND_CHASMS_MAGIC_DAMAGE =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "magic_damage");

    private AbnormalsDelightSilverKnifeCompat() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        if (!ModList.get().isLoaded("abnormals_delight")) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!ABNORMALS_DELIGHT_SILVER_KNIFE.equals(itemId)) {
            return;
        }

        // Ensure the held knife matches the intended attack speed.
        // Using vanilla's conventional modifier id so this acts as an override in modern versions.
        event.addModifier(
                Attributes.ATTACK_SPEED,
                new AttributeModifier(
                        ResourceLocation.withDefaultNamespace("base_attack_speed"),
                        DESIRED_ATTACK_SPEED_MODIFIER,
                        Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.MAINHAND
        );

        // Add exactly +0.5 magic damage (the item is removed from CC's magic_damage_items tag by our datapack).
        if (ModList.get().isLoaded("caverns_and_chasms")) {
            var magicDamage = BuiltInRegistries.ATTRIBUTE.getHolder(CAVERN_AND_CHASMS_MAGIC_DAMAGE).orElse(null);
            if (magicDamage == null) return;
            event.addModifier(
                    magicDamage,
                    new AttributeModifier(
                            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "silver_knife_magic_damage"),
                            1.0D,
                            Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
            );
        }
    }
}

