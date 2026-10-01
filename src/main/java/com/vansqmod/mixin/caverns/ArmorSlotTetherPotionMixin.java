package com.vansqmod.mixin.caverns;

import com.vansqmod.integration.tetherpotion.TetherPotionEquipment;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents wearable C&amp;C potions from being placed into vanilla armor slots (including helmet).
 * Equip is Curios {@code head} only.
 */
@Mixin(targets = "net.minecraft.world.inventory.ArmorSlot")
public abstract class ArmorSlotTetherPotionMixin {

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void vansqmod$denyPotionInArmorSlot(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (TetherPotionEquipment.isPotion(stack)) {
            cir.setReturnValue(false);
        }
    }
}
