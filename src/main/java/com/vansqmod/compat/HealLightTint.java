package com.vansqmod.compat;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

/**
 * HealLight tints {@code LivingEntityRenderer}'s main {@code renderToBuffer} green.
 * GeckoLib and overlay/outline layers never hit that call.
 */
public final class HealLightTint {

    public static final int COLOR = 0xFF3FFF3F;
    private static final Method GET_HEAL_TIME = findHealTime();

    private HealLightTint() {
    }

    public static boolean healing(LivingEntity entity) {
        if (GET_HEAL_TIME == null || entity == null) {
            return false;
        }
        try {
            return ((Integer) GET_HEAL_TIME.invoke(null, entity)) > 0;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static int boostLight(LivingEntity entity, int packedLight) {
        if (!healing(entity)) {
            return packedLight;
        }
        int block = Math.min(15, LightTexture.block(packedLight) + 3);
        int sky = Math.min(15, LightTexture.sky(packedLight) + 3);
        return LightTexture.pack(block, sky);
    }

    public static int color(LivingEntity entity, int fallback) {
        return healing(entity) ? COLOR : fallback;
    }

    private static Method findHealTime() {
        if (!ModList.get().isLoaded("healight")) {
            return null;
        }
        try {
            return Class.forName("dev.obscuria.healight.LivingExtension")
                    .getMethod("getHealTime", LivingEntity.class);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
