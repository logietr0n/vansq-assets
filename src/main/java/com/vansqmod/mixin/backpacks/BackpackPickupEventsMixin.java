package com.vansqmod.mixin.backpacks;

import com.spydnel.backpacks.common.events.BackpackPickupEvents;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Disables chest-slot pickup/place handlers; {@link com.vansqmod.integration.backpacks.BackpackCuriosIntegration} replaces them.
 */
@Mixin(BackpackPickupEvents.class)
public final class BackpackPickupEventsMixin {

    @Inject(method = "onRightClickBlock", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$skipChestPickupPlace(PlayerInteractEvent.RightClickBlock event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onRightClickItem", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$skipChestArmorSwap(PlayerInteractEvent.RightClickItem event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onItemEntityPickup", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$skipChestAutoEquip(ItemEntityPickupEvent.Pre event, CallbackInfo ci) {
        ci.cancel();
    }
}
