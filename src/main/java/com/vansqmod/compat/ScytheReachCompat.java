package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import com.vansqmod.registry.ModItemTags;
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
 * Scythes ({@link ModItemTags#SCYTHE}): shared combat stats.
 * <ul>
 *   <li>+1.0 entity interaction range (4.0 total), replacing Combat Nouveau's SwordItem +0.5</li>
 *   <li>0.8 attack speed</li>
 * </ul>
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class ScytheReachCompat {

    private static final ResourceLocation BASE_ENTITY_INTERACTION_RANGE_ID =
            ResourceLocation.withDefaultNamespace("base_entity_interaction_range");

    /** Vanilla base attack speed is 4.0; final = 4.0 + modifier → 0.8. */
    private static final double ATTACK_SPEED_MODIFIER = 0.8D - 4.0D;

    private ScytheReachCompat() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !stack.is(ModItemTags.SCYTHE)) {
            return;
        }

        event.replaceModifier(
                Attributes.ENTITY_INTERACTION_RANGE,
                new AttributeModifier(BASE_ENTITY_INTERACTION_RANGE_ID, 1.0D, Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
        );
        event.replaceModifier(
                Attributes.ATTACK_SPEED,
                new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, ATTACK_SPEED_MODIFIER, Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
        );
    }
}
