package com.vansqmod.mixin.beltborne;

import com.vansqmod.integration.beltborne.BeltborneLanternEquipment;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import top.theillusivec4.curios.api.SlotContext;

/**
 * Same lanterns must not merge into a belt curio. A different lantern may swap into that slot,
 * and a lantern may swap with a toolbelt or other belt item when no lantern is already worn.
 */
@Mixin(targets = "top.theillusivec4.curios.common.inventory.CurioSlot")
public abstract class CurioBeltLanternMayPlaceMixin extends SlotItemHandler {

    private CurioBeltLanternMayPlaceMixin(IItemHandler itemHandler, int index, int x, int y) {
        super(itemHandler, index, x, y);
    }

    @Shadow(remap = false)
    public abstract String getIdentifier();

    @Shadow(remap = false)
    public abstract SlotContext getSlotContext();

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (!BeltborneLanternEquipment.isLamp(stack)
                || !BeltborneLanternEquipment.BELT_SLOT.equals(getIdentifier())) {
            return super.mayPlace(stack);
        }
        if (!BeltborneLanternEquipment.canPlaceLanternInBelt(getSlotContext(), stack)) {
            return false;
        }
        return super.mayPlace(stack);
    }
}
