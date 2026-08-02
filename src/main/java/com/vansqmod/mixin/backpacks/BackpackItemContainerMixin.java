package com.vansqmod.mixin.backpacks;

import com.spydnel.backpacks.common.items.BackpackItemContainer;
import com.vansqmod.integration.backpacks.BackpackEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BackpackItemContainer.class)
public final class BackpackItemContainerMixin {

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack vansqmod$initFromBackSlot(LivingEntity entity, EquipmentSlot slot) {
        if (slot == EquipmentSlot.CHEST) {
            return BackpackEquipment.getEquippedBackpack(entity).orElse(ItemStack.EMPTY);
        }
        return entity.getItemBySlot(slot);
    }

    @Redirect(
            method = "setChanged",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack vansqmod$writeToBackSlot(LivingEntity entity, EquipmentSlot slot) {
        if (slot == EquipmentSlot.CHEST) {
            return BackpackEquipment.getEquippedBackpack(entity).orElse(ItemStack.EMPTY);
        }
        return entity.getItemBySlot(slot);
    }
}
