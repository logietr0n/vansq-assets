package com.vansqmod.mixin.client;

import com.vansqmod.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.AddAttributeTooltipsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Curios prints a gold {@code curios.modifiers.head} line ("When on head:") above curio
 * attribute modifiers. Crowns already get a light-gray head slot header from the
 * attribute modifiers Quark icons use, so skip Curios' copy for those items.
 */
@Mixin(targets = "top.theillusivec4.curios.client.ClientEventHandler", remap = false)
public abstract class CuriosAttributeSlotHeaderMixin {

    @Inject(method = "onAttributeTooltip", at = @At("HEAD"), cancellable = true)
    private void vansqmod$skipCrownSlotHeader(AddAttributeTooltipsEvent event, CallbackInfo ci) {
        ItemStack stack = event.getStack();
        if (stack.is(ModItems.GOLDEN_CROWN.get()) || stack.is(ModItems.SILVER_CROWN.get())) {
            ci.cancel();
        }
    }
}
