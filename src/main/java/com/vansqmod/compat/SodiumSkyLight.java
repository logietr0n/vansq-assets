package com.vansqmod.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;

/**
 * LambDynamicLights can write block-only light into Sodium's per-face cache with
 * sky=0. Smooth lighting then interpolates that onto nearby faces as view-angle
 * bright or dark spots. Restore real sky light from the world.
 */
public final class SodiumSkyLight {

    private static final ThreadLocal<BlockPos.MutableBlockPos> POS =
            ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

    private SodiumSkyLight() {
    }

    public static int maxWorldSky(BlockAndTintGetter level, int x, int y, int z, int fallback) {
        int sky = Mth.clamp(fallback, 0, 15);
        sky = sampleSky(level, x, y, z, sky);
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.level != level) {
            sky = sampleSky(minecraft.level, x, y, z, sky);
        }
        return Mth.clamp(sky, 0, 15);
    }

    private static int sampleSky(BlockAndTintGetter level, int x, int y, int z, int sky) {
        if (level == null) {
            return sky;
        }
        BlockPos.MutableBlockPos pos = POS.get();
        sky = Math.max(sky, level.getBrightness(LightLayer.SKY, pos.set(x, y, z)));
        sky = Math.max(sky, level.getBrightness(LightLayer.SKY, pos.set(x, y + 1, z)));
        return sky;
    }
}
