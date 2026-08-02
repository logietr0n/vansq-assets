package com.vansqmod.mixin.beltborne;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Backup guard: never process the belt-lantern toggle key on client tick if it was bound before this mod loaded.
 */
@Mixin(targets = "net.oxcodsnet.beltborne_lanterns.neoforge.client.BLNeoForgeClient$ClientBus", remap = false)
public class BeltborneClientToggleTickMixin {

    @Redirect(
            method = "onClientTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z",
                    ordinal = 3,
                    remap = true
            ),
            require = 1,
            remap = false
    )
    private static boolean vansqmod$ignoreToggleLanternClick(KeyMapping key) {
        if (key != null && "key.beltborne_lanterns.toggle_lantern".equals(key.getName())) {
            return false;
        }
        return key.consumeClick();
    }
}
