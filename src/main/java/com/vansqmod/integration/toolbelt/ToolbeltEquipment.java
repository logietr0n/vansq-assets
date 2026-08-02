package com.vansqmod.integration.toolbelt;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;

/**
 * Resolves the equipped Caverns &amp; Chasms toolbelt from Curios {@code belt}, not the vanilla legs slot.
 */
public final class ToolbeltEquipment {

    public static final String BELT_SLOT = "belt";
    public static final ResourceLocation TOOLBELT_ITEM =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "toolbelt");

    private ToolbeltEquipment() {
    }

    public static boolean isToolbelt(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return BuiltInRegistries.ITEM.getOptional(TOOLBELT_ITEM)
                .map(holder -> stack.is(holder))
                .orElse(false);
    }

    public static boolean hasToolbeltEquipped(LivingEntity entity) {
        if (entity == null || entity.level() == null) {
            return false;
        }
        return getEquippedToolbelt(entity).isPresent();
    }

    public static Optional<ItemStack> getEquippedToolbelt(LivingEntity entity) {
        return findToolbeltSlot(entity).map(SlotResult::stack);
    }

    public static Optional<SlotResult> findToolbeltSlot(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(inv -> inv.findFirstCurio(ToolbeltEquipment::isToolbelt));
    }

    public static void setEquippedToolbelt(LivingEntity entity, ItemStack stack) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return;
        }

        Optional<SlotResult> existing = findToolbeltSlot(entity);
        if (existing.isPresent()) {
            SlotContext ctx = existing.get().slotContext();
            handler.setEquippedCurio(ctx.identifier(), ctx.index(), stack);
            return;
        }

        handler.getStacksHandler(BELT_SLOT).ifPresent(beltHandler -> {
            IDynamicStackHandler stacks = beltHandler.getStacks();
            int slotCount = stacks.getSlots();
            for (int i = 0; i < slotCount; i++) {
                if (stacks.isItemValid(i, stack)) {
                    handler.setEquippedCurio(BELT_SLOT, i, stack);
                    return;
                }
            }
            if (slotCount > 0) {
                handler.setEquippedCurio(BELT_SLOT, 0, stack);
            }
        });
    }
}
