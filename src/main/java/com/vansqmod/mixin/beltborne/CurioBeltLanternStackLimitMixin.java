package com.vansqmod.mixin.beltborne;

import com.vansqmod.integration.beltborne.BeltborneLanternEquipment;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import top.theillusivec4.curios.api.SlotContext;

import java.util.function.Function;

/**
 * Curios belt slots otherwise use the item's max stack size, so a handful of lanterns
 * could sit in one slot. Cap lanterns at 1; toolbelts are unaffected.
 */
@Mixin(targets = "top.theillusivec4.curios.common.inventory.DynamicStackHandler")
public abstract class CurioBeltLanternStackLimitMixin extends ItemStackHandler {

    @Shadow(remap = false)
    protected Function<Integer, SlotContext> ctxBuilder;

    private CurioBeltLanternStackLimitMixin() {
        super(0);
    }

    @Override
    public int getStackLimit(int slot, ItemStack stack) {
        try {
            if (vansqmod$isBeltLantern(slot, stack)) {
                return 1;
            }
        } catch (Throwable ignored) {
            // Inventory rendering must not break if Curios context is mid-rebuild.
        }
        return super.getStackLimit(slot, stack);
    }

    @Unique
    private boolean vansqmod$isBeltLantern(int slot, ItemStack stack) {
        Function<Integer, SlotContext> builder = this.ctxBuilder;
        if (builder == null || stack == null || stack.isEmpty()) {
            return false;
        }
        SlotContext ctx = builder.apply(slot);
        return ctx != null
                && BeltborneLanternEquipment.BELT_SLOT.equals(ctx.identifier())
                && BeltborneLanternEquipment.isLamp(stack);
    }
}
