package com.vansqmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.awt.Color;

/**
 * Day/night visibility for Distant Horizons LOD clouds.
 * <p>
 * Smooth fade is done by lerping cloud RGB toward the current sky color
 * (works even when the render path ignores alpha). At full night, rendering
 * is disabled entirely via {@code setActive(false)}.
 */
@OnlyIn(Dist.CLIENT)
public final class DhCloudDayNight {

    /** Sun height where clouds are fully visible. */
    private static final float FULL_DAY_SUN = 0.2F;
    /** Sun height where clouds are fully hidden. */
    private static final float FULL_NIGHT_SUN = -0.05F;
    /** Quantize so DH's color-equality cache is not invalidated every frame. */
    private static final float VISIBILITY_STEPS = 32.0F;

    private DhCloudDayNight() {
    }

    /**
     * @param timeOfDay {@link ClientLevel#getTimeOfDay(float)}
     * @return cloud visibility in {@code [0, 1]}
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

    /**
     * Blends cloud color toward sky color as visibility drops.
     * Opaque RGB match hides clouds against the sky without needing alpha blend.
     */
    public static Color applyFade(Color cloud, ClientLevel level, float partialTick) {
        float visibility = visibility(level.getTimeOfDay(partialTick));
        if (visibility >= 0.999F) {
            return cloud;
        }

        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 sky = level.getSkyColor(camera, partialTick);

        // visibility 1 → cloud color; visibility 0 → sky color
        int r = Mth.clamp(Math.round(Mth.lerp(visibility, (float) sky.x, cloud.getRed() / 255.0F) * 255.0F), 0, 255);
        int g = Mth.clamp(Math.round(Mth.lerp(visibility, (float) sky.y, cloud.getGreen() / 255.0F) * 255.0F), 0, 255);
        int b = Mth.clamp(Math.round(Mth.lerp(visibility, (float) sky.z, cloud.getBlue() / 255.0F) * 255.0F), 0, 255);
        // Keep alpha as a bonus for render paths that honor it; RGB sky-match is the real fade.
        int a = Mth.clamp(Math.round(visibility * 255.0F), 0, 255);

        if (cloud.getRed() == r && cloud.getGreen() == g && cloud.getBlue() == b && cloud.getAlpha() == a) {
            return cloud;
        }
        return new Color(r, g, b, a);
    }
}
