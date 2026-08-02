package com.vansqmod.mixin.quark;

import com.vansqmod.registry.ModItemTags;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Treat {@code #vansqmod:scythe} as a hoe for Quark's area harvest / simple-harvest range,
 * without adding scythes to {@code #minecraft:hoes}.
 */
@Mixin(targets = "org.violetmoon.quark.content.tweaks.module.HoeHarvestingModule", remap = false)
public abstract class HoeHarvestingScytheMixin {

    @Inject(method = "isHoe", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$scytheCountsAsHoe(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.isEmpty() && stack.is(ModItemTags.SCYTHE)) {
            cir.setReturnValue(true);
        }
    }
}
