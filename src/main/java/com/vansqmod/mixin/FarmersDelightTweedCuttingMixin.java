package com.vansqmod.mixin;

import com.vansqmod.compat.TweedDrops;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Cutting-board straw results (rice, flax, etc.) use the same 1/8196 tweed swap.
 */
@Mixin(targets = "vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe", remap = false)
public class FarmersDelightTweedCuttingMixin {

    @Inject(method = "rollResults", at = @At("RETURN"))
    private void vansqmod$maybeTweed(
            RandomSource random,
            int fortuneLevel,
            RecipeWrapper wrapper,
            CallbackInfoReturnable<List<ItemStack>> cir) {
        TweedDrops.replaceInList(cir.getReturnValue(), random);
    }
}
