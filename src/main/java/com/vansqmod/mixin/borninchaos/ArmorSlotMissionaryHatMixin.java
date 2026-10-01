package com.vansqmod.mixin.borninchaos;

import com.vansqmod.integration.missionaryhat.MissionaryHatEquipment;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents the Missionary hat from being placed into vanilla armor slots (including helmet).
 * Equip is Curios {@code head} only.
 */
@Mixin(targets = "net.minecraft.world.inventory.ArmorSlot")
public abstract class ArmorSlotMissionaryHatMixin {

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void vansqmod$denyHatInArmorSlot(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (MissionaryHatEquipment.isHat(stack)) {
            cir.setReturnValue(false);
        }
    }
}
