package com.vansqmod.integration.missionaryhat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;

/**
 * Resolves {@code born_in_chaos_v1:missionary_hat_helmet} from Curios {@code head} only.
 */
public final class MissionaryHatEquipment {

    public static final String HEAD_SLOT = "head";
    public static final ResourceLocation HAT_ITEM =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "missionary_hat_helmet");

    private MissionaryHatEquipment() {
    }

    public static boolean isHat(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return BuiltInRegistries.ITEM.getOptional(HAT_ITEM).map(stack::is).orElse(false);
    }

    public static boolean isHatItem(Item item) {
        return item != null && HAT_ITEM.equals(BuiltInRegistries.ITEM.getKey(item));
    }

    public static boolean isWearingHat(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        if (getCuriosHat(entity).isPresent()) {
            return true;
        }
        // Same-tick fallback while a helmet-slot stack is being moved to Curios.
        return isHat(entity.getItemBySlot(EquipmentSlot.HEAD));
    }

    public static boolean hasHatEquipped(LivingEntity entity) {
        return entity != null && entity.level() != null && getCuriosHat(entity).isPresent();
    }

    public static Optional<ItemStack> getCuriosHat(LivingEntity entity) {
        return findHatSlot(entity).map(SlotResult::stack);
    }

    public static Optional<SlotResult> findHatSlot(LivingEntity entity) {
        if (entity == null || !ModList.get().isLoaded("curios")) {
            return Optional.empty();
        }
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(inv -> inv.findFirstCurio(MissionaryHatEquipment::isHat))
                .filter(result -> HEAD_SLOT.equals(result.slotContext().identifier()));
    }

    /**
     * Puts the hat into an empty Curios {@code head} slot. Does not overwrite another head curio.
     *
     * @return {@code true} if the hat is now in Curios head
     */
    public static boolean tryEquipHat(LivingEntity entity, ItemStack stack) {
        if (entity == null || stack.isEmpty() || !isHat(stack)) {
            return false;
        }
        if (getCuriosHat(entity).isPresent()) {
            return false;
        }
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return false;
        }
        return handler.getStacksHandler(HEAD_SLOT).map(headHandler -> {
            IDynamicStackHandler stacks = headHandler.getStacks();
            int slots = stacks.getSlots();
            for (int i = 0; i < slots; i++) {
                if (stacks.getStackInSlot(i).isEmpty() && stacks.isItemValid(i, stack)) {
                    handler.setEquippedCurio(HEAD_SLOT, i, stack);
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }

    public static void setEquippedHat(LivingEntity entity, ItemStack stack) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return;
        }

        Optional<SlotResult> existing = findHatSlot(entity);
        if (existing.isPresent()) {
            SlotContext ctx = existing.get().slotContext();
            handler.setEquippedCurio(ctx.identifier(), ctx.index(), stack);
            return;
        }

        handler.getStacksHandler(HEAD_SLOT).ifPresent(headHandler -> {
            IDynamicStackHandler stacks = headHandler.getStacks();
            int slots = stacks.getSlots();
            for (int i = 0; i < slots; i++) {
                if (stacks.getStackInSlot(i).isEmpty() && stacks.isItemValid(i, stack)) {
                    handler.setEquippedCurio(HEAD_SLOT, i, stack);
                    return;
                }
            }
        });
    }
}
