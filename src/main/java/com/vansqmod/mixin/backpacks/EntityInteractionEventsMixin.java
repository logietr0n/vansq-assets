package com.vansqmod.mixin.backpacks;

import com.spydnel.backpacks.common.events.EntityInteractionEvents;
import com.vansqmod.integration.backpacks.BackpackEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityInteractionEvents.class)
public final class EntityInteractionEventsMixin {

    @Redirect(
            method = "onEntityInteract",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private static ItemStack vansqmod$readBackpackFromBack(LivingEntity entity, EquipmentSlot slot) {
        if (slot == EquipmentSlot.CHEST) {
            return BackpackEquipment.getEquippedBackpack(entity).orElse(ItemStack.EMPTY);
        }
        return entity.getItemBySlot(slot);
    }
}
