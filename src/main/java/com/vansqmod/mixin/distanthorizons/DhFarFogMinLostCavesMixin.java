package com.vansqmod.mixin.distanthorizons;

import com.seibel.distanthorizons.core.config.Config;
import com.vansqmod.client.LostCavesDhFogClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes Distant Horizons {@code Fog Min} read as the live underground override
 * while {@link LostCavesDhFogClient} is active. Uses {@code get()} so the saved
 * config file is not rewritten.
 */
@Mixin(
        targets = "com.seibel.distanthorizons.core.config.types.ConfigEntry",
        remap = false
)
public abstract class DhFarFogMinLostCavesMixin {

    @Inject(method = "get()Ljava/lang/Object;", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$overrideFarFogMinInLostCaves(CallbackInfoReturnable<Object> cir) {
        try {
            if (!LostCavesDhFogClient.shouldOverrideFogMin()) {
                return;
            }
            if (this == (Object) Config.Client.Advanced.Graphics.Fog.farFogMin) {
                cir.setReturnValue(LostCavesDhFogClient.fogMinOverride());
            }
        } catch (Throwable ignored) {
            // Title screen / DH init must not break if the client class is not ready.
        }
    }
}
