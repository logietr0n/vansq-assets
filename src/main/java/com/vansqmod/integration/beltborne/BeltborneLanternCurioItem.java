package com.vansqmod.integration.beltborne;

import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * Allows right-click equipping Beltborne lamp items into the Curios belt slot.
 */
public final class BeltborneLanternCurioItem implements ICurioItem {

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return BeltborneLanternEquipment.BELT_SLOT.equals(slotContext.identifier())
                && BeltborneLanternEquipment.isLamp(stack);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return canEquip(slotContext, stack);
    }
}
