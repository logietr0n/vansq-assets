package com.vansqmod.mixin;

import com.vansqmod.entity.PigLitters;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Golden carrots breed pigs (Quark Pig Litters parity). */
@Mixin(Pig.class)
public class PigGoldenCarrotFoodMixin {

    @Inject(method = "isFood", at = @At("HEAD"), cancellable = true)
    private void vansqmod$goldenCarrotIsFood(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.isEmpty() && stack.is(Items.GOLDEN_CARROT)) {
            cir.setReturnValue(true);
        }
    }
}
