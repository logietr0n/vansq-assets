package com.vansqmod.client;

import com.vansqmod.integration.curios.CurioSpyglass;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

import java.lang.reflect.Field;

/**
 * Vanilla {@link net.minecraft.world.entity.player.Player#isScoping()} plus Spyglass
 * Improvements' {@code force_spyglass} keybind zoom.
 */
public final class SpyglassZooming {

    private static final String SPYGLASS_IMPROVEMENTS_CLIENT =
            "me.juancarloscp52.spyglass_improvements.client.SpyglassImprovementsClient";

    private static Field forceSpyglass;
    private static boolean forceSpyglassResolved;

    private SpyglassZooming() {
    }

    public static boolean isZooming() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.isScoping()) {
            return true;
        }
        return isImprovementsZoom();
    }

    /**
     * Spyglass Improvements keybind zoom. Do not call {@link #isZooming()} from an
     * {@code isScoping} mixin — that recurses.
     */
    public static boolean isImprovementsZoom() {
        return isSpyglassImprovementsZooming();
    }

    public static boolean isHoldingOrWearingSpyglass() {
        if (isImprovementsZoom()) {
            return true;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }
        if (mc.player.getMainHandItem().is(Items.SPYGLASS) || mc.player.getOffhandItem().is(Items.SPYGLASS)) {
            return true;
        }
        return hasCurioSpyglass(mc.player);
    }

    public static boolean hasCurioSpyglass(LivingEntity entity) {
        return CurioSpyglass.has(entity);
    }

    private static boolean isSpyglassImprovementsZooming() {
        if (!forceSpyglassResolved) {
            forceSpyglassResolved = true;
            try {
                forceSpyglass = Class.forName(SPYGLASS_IMPROVEMENTS_CLIENT).getField("force_spyglass");
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        }
        if (forceSpyglass == null) {
            return false;
        }
        try {
            return forceSpyglass.getBoolean(null);
        } catch (IllegalAccessException ignored) {
            return false;
        }
    }
}
