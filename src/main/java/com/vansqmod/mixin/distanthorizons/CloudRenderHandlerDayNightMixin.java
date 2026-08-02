package com.vansqmod.mixin.distanthorizons;

import com.vansqmod.client.DhCloudDayNight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Hard-hides Distant Horizons LOD clouds at night.
 * Alpha fading alone is unreliable with some render paths / shader packs;
 * forcing {@code setActive(false)} skips drawing entirely.
 */
@Mixin(
        targets = "com.seibel.distanthorizons.core.render.renderer.CloudRenderHandler",
        remap = false
)
public abstract class CloudRenderHandlerDayNightMixin {

    @ModifyArg(
            method = "preRender",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/seibel/distanthorizons/api/interfaces/render/IDhApiRenderableBoxGroup;setActive(Z)V",
                    ordinal = 0
            ),
            index = 0,
            remap = false
    )
    private boolean vansqmod$hideCloudsAtNight(boolean active) {
        if (!active) {
            return false;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !level.dimensionType().hasSkyLight()) {
            return true;
        }

        return DhCloudDayNight.shouldRender(level.getTimeOfDay(0.0F));
    }
}
