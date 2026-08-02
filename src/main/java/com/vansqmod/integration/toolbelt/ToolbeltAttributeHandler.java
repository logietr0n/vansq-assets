package com.vansqmod.integration.toolbelt;

import com.vansqmod.VansqMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/**
 * Applies toolbelt block reach on the entity. Must run on both logical sides ({@code curioTick});
 * {@link top.theillusivec4.curios.api.event.CurioChangeEvent} is server-only, which is why Extending
 * appeared to work in the main hand (client applies held-item enchants) but not on the belt slot.
 */
public final class ToolbeltAttributeHandler {

    private static final ResourceLocation BASE_RANGE_ID =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "toolbelt_block_range");
    private static final ResourceLocation EXTENDING_RANGE_ID =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "toolbelt_extending");

    private ToolbeltAttributeHandler() {
    }

    public static void refresh(LivingEntity entity) {
        refresh(entity, ItemStack.EMPTY);
    }

    public static void refresh(LivingEntity entity, ItemStack stackInChangedSlot) {
        clear(entity);
        if (ToolbeltEquipment.isToolbelt(stackInChangedSlot)) {
            apply(entity, stackInChangedSlot);
            return;
        }
        ToolbeltEquipment.getEquippedToolbelt(entity).ifPresent(stack -> apply(entity, stack));
    }

    /** Idempotent; safe every tick from {@code curioTick} on client and server. */
    public static void syncEquipped(LivingEntity entity, ItemStack beltStack) {
        if (!ToolbeltEquipment.isToolbelt(beltStack)) {
            clear(entity);
            return;
        }
        apply(entity, beltStack);
    }

    private static void apply(LivingEntity entity, ItemStack stack) {
        AttributeInstance reach = entity.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (reach == null) {
            return;
        }
        reach.addOrUpdateTransientModifier(
                new AttributeModifier(BASE_RANGE_ID, 2.0D, AttributeModifier.Operation.ADD_VALUE)
        );
        int extending = ToolbeltEnchantmentSupport.getExtendingLevel(stack);
        if (extending > 0) {
            reach.addOrUpdateTransientModifier(
                    new AttributeModifier(
                            EXTENDING_RANGE_ID,
                            ToolbeltEnchantmentSupport.extendingRangeBonus(extending),
                            AttributeModifier.Operation.ADD_VALUE
                    )
            );
        } else {
            reach.removeModifier(EXTENDING_RANGE_ID);
        }
    }

    public static void clear(LivingEntity entity) {
        AttributeInstance reach = entity.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (reach == null) {
            return;
        }
        reach.removeModifier(BASE_RANGE_ID);
        reach.removeModifier(EXTENDING_RANGE_ID);
    }
}
