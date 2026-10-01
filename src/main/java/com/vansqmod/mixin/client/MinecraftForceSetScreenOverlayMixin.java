package com.vansqmod.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@link Minecraft#forceSetScreen} nests {@code runTick} so the new screen draws immediately.
 * Distant Horizons does that from Quick Play while {@link net.minecraft.client.gui.screens.LoadingOverlay}
 * is still rendering, which re-enters refraction's ImGui {@code beginFrame} and aborts
 * ({@code 0xc0000409}). Swap the screen without the nested tick; the overlay keeps fading.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftForceSetScreenOverlayMixin {

    @Shadow
    public abstract Overlay getOverlay();

    @Shadow
    public abstract void setScreen(Screen screen);

    @Inject(method = "forceSetScreen", at = @At("HEAD"), cancellable = true)
    private void vansqmod$noNestedTickDuringOverlay(Screen screen, CallbackInfo ci) {
        if (this.getOverlay() != null) {
            this.setScreen(screen);
            ci.cancel();
        }
    }
}
