package com.vansqmod.integration.beltborne;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.oxcodsnet.beltborne_lanterns.common.LampRegistry;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;

/**
 * Resolves belt lamps from the Curios {@code belt} slot for Beltborne Lanterns rendering and light.
 */
public final class BeltborneLanternEquipment {

    public static final String BELT_SLOT = "belt";
    public static final TagKey<Item> LAMPS_TAG = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("beltborne_lanterns", "lamps")
    );

    private BeltborneLanternEquipment() {
    }

    public static boolean isLamp(ItemStack stack) {
        return !stack.isEmpty() && LampRegistry.isLamp(stack);
    }

    public static Optional<ItemStack> getBeltLamp(LivingEntity entity) {
        return findBeltLampSlot(entity).map(SlotResult::stack);
    }

    public static Optional<SlotResult> findBeltLampSlot(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.findCurios(BELT_SLOT).stream()
                        .filter(result -> isLamp(result.stack()))
                        .findFirst());
    }

    public static void setBeltLamp(LivingEntity entity, ItemStack stack) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return;
        }

        Optional<SlotResult> existing = findBeltLampSlot(entity);
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
