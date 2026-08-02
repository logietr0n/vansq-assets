package com.vansqmod.mixin.distanthorizons;

import com.vansqmod.client.DhCloudDayNight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.Color;

/**
 * Smoothly fades Distant Horizons LOD clouds by lerping their color toward the sky.
 * Nighttime hide is handled by {@link CloudRenderHandlerDayNightMixin}.
 */
@Mixin(
        targets = "com.seibel.distanthorizons.common.wrappers.world.ClientLevelWrapper_neoforge",
        remap = false
)
public abstract class ClientLevelWrapperCloudFadeMixin {

    @Inject(method = "getCloudColor", at = @At("RETURN"), cancellable = true, remap = false)
    private void vansqmod$fadeCloudsWithDayNight(float partialTick, CallbackInfoReturnable<Color> cir) {
        Color color = cir.getReturnValue();
        if (color == null) {
            return;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !level.dimensionType().hasSkyLight()) {
            return;
        }

        Color faded = DhCloudDayNight.applyFade(color, level, partialTick);
        if (faded != color) {
            cir.setReturnValue(faded);
        }
    }
}
