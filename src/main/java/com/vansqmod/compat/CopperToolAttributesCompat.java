package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * Overrides vanilla {@code minecraft:copper_*} tool combat stats.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class CopperToolAttributesCompat {

    private CopperToolAttributesCompat() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!ResourceLocation.DEFAULT_NAMESPACE.equals(itemId.getNamespace())) {
            return;
        }

        switch (itemId.getPath()) {
            case "copper_sword" -> replaceAttackDamage(event, 5.5D);
            case "copper_pickaxe" -> replaceAttackDamage(event, 3.5D);
            case "copper_shovel" -> replaceAttackDamage(event, 4.0D);
            case "copper_axe" -> replaceAttackSpeed(event, 0.85D);
            case "copper_hoe" -> replaceAttackSpeed(event, 2.5D);
            default -> {
            }
        }
    }

    /** {@code createAttributes} merges tier bonus into one ADD_VALUE modifier; final damage = 1 + this value. */
    private static void replaceAttackDamage(ItemAttributeModifierEvent event, double desiredDamage) {
        event.replaceModifier(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, desiredDamage - 1.0D, Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
        );
    }

    /** Vanilla base attack speed is 4.0; final speed = 4 + modifier. */
    private static void replaceAttackSpeed(ItemAttributeModifierEvent event, double desiredSpeed) {
        event.replaceModifier(
                Attributes.ATTACK_SPEED,
                new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, desiredSpeed - 4.0D, Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
        );
    }
}
