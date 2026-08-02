package com.vansqmod.mixin.backpacks;

import com.spydnel.backpacks.registry.BPItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents backpacks from being placed into vanilla armor slots (including chest).
 * Equip is Curios {@code back} only.
 */
@Mixin(targets = "net.minecraft.world.inventory.ArmorSlot")
public abstract class ArmorSlotBackpackMixin {

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void vansqmod$denyBackpackInArmorSlot(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(BPItems.BACKPACK)) {
            cir.setReturnValue(false);
        }
    }
}
