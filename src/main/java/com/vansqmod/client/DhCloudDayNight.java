package com.vansqmod.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.awt.Color;

/**
 * Day/night opacity for Distant Horizons LOD clouds.
 * <p>
 * Opacity is a function of {@link ClientLevel#getTimeOfDay(float)} only, so
 * skipping the clock lands on the alpha that moment would have had. At full
 * night, rendering is disabled via {@code setActive(false)}.
 * <p>
 * The box color Distant Horizons uploaded is left alone. Changing it at the
 * start of the fade rebuilds the mesh a frame late and draws the old solid
 * white clouds once. Alpha is a shader uniform, applied after the lightmap
 * and face shading.
 */
@OnlyIn(Dist.CLIENT)
public final class DhCloudDayNight {

    /**
     * Sun height where clouds are fully opaque. The window is half as wide as
     * the previous dusk fade, centered on the same part of the day.
     */
    private static final float FULL_DAY_SUN = 0.325F;
    /** Sun height where clouds are fully hidden. */
    private static final float FULL_NIGHT_SUN = -0.125F;
    /** Quantize so the fade steps stay stable within a tick. */
    private static final float VISIBILITY_STEPS = 64.0F;
    /** Fully lit clouds stay slightly see-through. The fade scales down from this. */
    private static final float MAX_OPACITY = 0.8F;

    private static float renderedVisibility = 1.0F;
    /** Sky dimensions draw clouds in the transparent layer. Dimensions without a sky do not. */
    private static boolean layerActive;

    private DhCloudDayNight() {
    }

    /**
     * @param timeOfDay {@link ClientLevel#getTimeOfDay(float)}
     * @return cloud opacity in {@code [0, 1]}
     */
    public static float visibility(float timeOfDay) {
        float sun = Mth.cos(timeOfDay * ((float) Math.PI * 2.0F));
        float v = Mth.inverseLerp(sun, FULL_NIGHT_SUN, FULL_DAY_SUN);
        v = Mth.clamp(v, 0.0F, 1.0F);
        return Math.round(v * VISIBILITY_STEPS) / VISIBILITY_STEPS;
    }

    /** Whether DH cloud box groups should draw at all. */
    public static boolean shouldRender(float timeOfDay) {
        return visibility(timeOfDay) > 0.0F;
    }

    /** Visibility sampled from the same tick the cloud color was read. */
    public static float renderedVisibility() {
        return renderedVisibility;
    }

    /** Dimensions without a sky keep the normal opaque clouds. */
    public static void showOpaque() {
        renderedVisibility = 1.0F;
        layerActive = false;
    }

    /** Sky clouds are drawn in the single transparent layer, capped at {@link #MAX_OPACITY}. */
    public static boolean usesLayer() {
        return layerActive && renderedVisibility > 0.0F;
    }

    /** Opacity for the cloud layer, from fully hidden up to {@link #MAX_OPACITY}. */
    public static float layerAlpha() {
        return renderedVisibility * MAX_OPACITY;
    }

    /**
     * Records the fade for this frame and returns {@code cloud} unchanged.
     * Alpha is applied later, once, for the whole cloud layer.
     */
    public static Color applyFade(Color cloud, ClientLevel level, float partialTick) {
        renderedVisibility = visibility(level.getTimeOfDay(partialTick));
        layerActive = true;
        return cloud;
    }
}
