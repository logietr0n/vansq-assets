package com.vansqmod.mixin.beltborne;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Prevents registering Beltborne's B-key toggle when lamps are equipped through Curios.
 */
@Mixin(targets = "net.oxcodsnet.beltborne_lanterns.neoforge.client.BLNeoForgeClient", remap = false)
public class BeltborneSuppressToggleKeyMixin {

    @Redirect(
            method = "registerKeys",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/client/event/RegisterKeyMappingsEvent;register(Lnet/minecraft/client/KeyMapping;)V",
                    ordinal = 3,
                    remap = false
            ),
            require = 1,
            remap = false
    )
    private static void vansqmod$skipToggleLanternKey(RegisterKeyMappingsEvent event, KeyMapping mapping) {
        if ("key.beltborne_lanterns.toggle_lantern".equals(mapping.getName())) {
            return;
        }
        event.register(mapping);
    }
}
