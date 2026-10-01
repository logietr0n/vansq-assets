package com.vansqmod.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.ModList;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Jaden's Nether Expansion only adds {@code BurningFilterLayer} to
 * {@code LivingEntityRenderer} parent models. Geo mobs and outline layers skip it.
 */
public final class JneBurning {

    private static final boolean LOADED = ModList.get().isLoaded("netherexp");
    private static final TagKey<EntityType<?>> NO_FILTER = findNoFilterTag();
    private static final Supplier<Boolean> ENABLED = findEnabled();
    private static final Supplier<?> LAST_FIRE = findLastFire();
    private static final Method GET_ROW = findRow();
    private static final Method GET_PALETTE = findPalette();
    private static final Method FIRE_OVERLAY = findFireOverlay();

    private JneBurning() {
    }

    public static boolean shouldDraw(LivingEntity entity) {
        if (!LOADED || entity == null || entity.isSpectator() || !entity.displayFireAnimation()) {
            return false;
        }
        if (NO_FILTER != null && entity.getType().is(NO_FILTER)) {
            return false;
        }
        return ENABLED == null || Boolean.TRUE.equals(ENABLED.get());
    }

    public static int glowColor(LivingEntity entity, float partialTick) {
        int rgb = 0xFF6A00;
        ResourceLocation fire = lastFire(entity);
        if (fire != null && GET_ROW != null && GET_PALETTE != null) {
            try {
                int row = (Integer) GET_ROW.invoke(null, fire);
                Color palette = (Color) GET_PALETTE.invoke(null, row, 2);
                if (palette != null) {
                    rgb = palette.getRGB() & 0xFFFFFF;
                }
            } catch (Throwable ignored) {
            }
        }
        return pulse(entity, partialTick, rgb);
    }

    public static RenderType fireOverlay(ResourceLocation texture) {
        if (FIRE_OVERLAY == null || texture == null) {
            return null;
        }
        try {
            Object type = FIRE_OVERLAY.invoke(null, texture);
            return type instanceof RenderType renderType ? renderType : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static void renderModel(
            EntityModel<?> model,
            ResourceLocation texture,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            LivingEntity entity,
            float partialTick
    ) {
        if (!shouldDraw(entity) || model == null) {
            return;
        }
        RenderType type = fireOverlay(texture);
        if (type == null) {
            return;
        }
        model.renderToBuffer(
                poseStack,
                buffer.getBuffer(type),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                glowColor(entity, partialTick)
        );
    }

    private static int pulse(LivingEntity entity, float partialTick, int rgb) {
        float min = 0.05F;
        float max = 0.5F;
        float speed = 0.4F;
        float step = 1.6666666F;
        float t = Mth.floor((entity.tickCount + partialTick) / step) * step;
        float mid = (min + max) / 2.0F;
        float amp = (max - min) / 2.0F;
        float alpha = Mth.clamp(mid + amp * Mth.sin(t * speed), min, max);
        return ((int) (alpha * 255.0F) << 24) | rgb;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ResourceLocation lastFire(LivingEntity entity) {
        if (LAST_FIRE == null) {
            return null;
        }
        try {
            Object value = entity.getData((Supplier) LAST_FIRE);
            return value instanceof ResourceLocation location ? location : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static TagKey<EntityType<?>> findNoFilterTag() {
        if (!LOADED) {
            return null;
        }
        try {
            Field field = Class.forName("net.jadenxgamer.netherexp.core.keys.JNETags$EntityTypes")
                    .getField("NO_BURNING_FILTER");
            return (TagKey<EntityType<?>>) field.get(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Supplier<Boolean> findEnabled() {
        if (!LOADED) {
            return () -> true;
        }
        try {
            Object value = Class.forName("net.jadenxgamer.netherexp.config.JNEConfigs")
                    .getField("MOB_BURNING_GLOW")
                    .get(null);
            Method get = value.getClass().getMethod("get");
            return () -> {
                try {
                    Object result = get.invoke(value);
                    return result instanceof Boolean bool ? bool : Boolean.TRUE.equals(result);
                } catch (Throwable ignored) {
                    return true;
                }
            };
        } catch (Throwable ignored) {
            return () -> true;
        }
    }

    @SuppressWarnings("unchecked")
    private static Supplier<?> findLastFire() {
        if (!LOADED) {
            return null;
        }
        try {
            return (Supplier<?>) Class.forName("net.jadenxgamer.netherexp.registry.JNEAttachmentTypes")
                    .getField("LAST_FIRE")
                    .get(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Method findRow() {
        if (!LOADED) {
            return null;
        }
        try {
            return Class.forName("net.jadenxgamer.netherexp.client.assetdriven.managers.BurnPalettesManager")
                    .getMethod("getRowForBlock", ResourceLocation.class);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Method findPalette() {
        if (!LOADED) {
            return null;
        }
        try {
            return Class.forName("net.jadenxgamer.netherexp.client.assetdriven.managers.BurnPalettesManager")
                    .getMethod("getPaletteColor", int.class, int.class);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Method findFireOverlay() {
        if (!LOADED) {
            return null;
        }
        try {
            return Class.forName("net.jadenxgamer.netherexp.client.rendering.JNERenderType")
                    .getMethod("fireOverlay", ResourceLocation.class);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
