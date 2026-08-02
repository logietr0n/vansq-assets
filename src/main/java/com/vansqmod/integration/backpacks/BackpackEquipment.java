package com.vansqmod.integration.backpacks;

import com.spydnel.backpacks.registry.BPItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;

/**
 * Resolves the equipped Backpacks backpack from Curios {@code back}, not the vanilla chest slot.
 */
public final class BackpackEquipment {

    public static final String BACK_SLOT = "back";

    private BackpackEquipment() {
    }

    public static boolean isBackpack(ItemStack stack) {
        return !stack.isEmpty() && stack.is(BPItems.BACKPACK);
    }

    public static boolean hasBackpackEquipped(LivingEntity entity) {
        if (entity == null || entity.level() == null) {
            return false;
        }
        return getEquippedBackpack(entity).isPresent();
    }

    public static Optional<ItemStack> getEquippedBackpack(LivingEntity entity) {
        return findBackpackSlot(entity).map(SlotResult::stack);
    }

    public static Optional<SlotResult> findBackpackSlot(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(inv -> inv.findFirstCurio(BackpackEquipment::isBackpack));
    }

    public static void setEquippedBackpack(LivingEntity entity, ItemStack stack) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return;
        }

        Optional<SlotResult> existing = findBackpackSlot(entity);
        if (existing.isPresent()) {
            SlotContext ctx = existing.get().slotContext();
            handler.setEquippedCurio(ctx.identifier(), ctx.index(), stack);
            return;
        }

        handler.getStacksHandler(BACK_SLOT).ifPresent(backHandler -> {
            IDynamicStackHandler stacks = backHandler.getStacks();
            int slots = stacks.getSlots();
            for (int i = 0; i < slots; i++) {
                if (stacks.isItemValid(i, stack)) {
                    handler.setEquippedCurio(BACK_SLOT, i, stack);
                    return;
                }
            }
            if (slots > 0) {
                handler.setEquippedCurio(BACK_SLOT, 0, stack);
            }
        });
    }
}
