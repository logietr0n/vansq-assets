package com.vansqmod.mixin.distanthorizons;

import com.vansqmod.client.SandstormVanillaFogClient;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Distant Horizons cancels vanilla terrain fog at {@code FogRenderer.setupFog} RETURN,
 * which runs after YUNG's sandstorm fog and wipes it. Force {@code cancelFog} false while
 * Lost Caves sandstorm fog is needed so YCB's distances stick.
 * <p>
 * More reliable than the config API alone — DH's RETURN inject still wins over YCB's TAIL
 * even when {@code enableVanillaFog} is toggled late or the API override is ignored.
 */
@Mixin(
        targets = "com.seibel.distanthorizons.common.commonMixins.MixinVanillaFogCommon_neoforge",
        remap = false
)
public abstract class DhCancelFogSandstormMixin {

    @Inject(method = "cancelFog", at = @At("HEAD"), cancellable = true, remap = false)
    private static void vansqmod$keepSandstormFog(
            Camera camera,
            FogRenderer.FogMode fogMode,
            CallbackInfoReturnable<Boolean> cir
    ) {
        // Flag only — never touch Minecraft/DH APIs from this hot fog path.
        try {
            if (SandstormVanillaFogClient.shouldAllowVanillaFog()) {
                cir.setReturnValue(false);
            }
        } catch (Throwable ignored) {
            // FancyMenu / title-screen render must not break if client class isn't ready.
        }
    }
}
