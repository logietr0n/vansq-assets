package com.vansqmod.mixin.shieldexp;

import net.minecraft.world.item.Item;
import org.infernalstudios.shieldexp.config.ShieldExpansionConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tallestred.piglinproliferation.common.items.BucklerItem;

/**
 * Keep Piglin Proliferation bucklers out of Shield Expansion's shield list,
 * parry window, and off-guard cooldown.
 */
@Mixin(value = ShieldExpansionConfig.class, remap = false)
public abstract class ShieldExpansionBucklerMixin {

    @Inject(method = "isShield", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$excludeBuckler(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (item instanceof BucklerItem) {
            cir.setReturnValue(false);
        }
    }
}
