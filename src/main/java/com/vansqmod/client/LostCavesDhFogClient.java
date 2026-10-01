package com.vansqmod.client;

import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.enums.rendering.EDhApiFogColorMode;
import com.seibel.distanthorizons.api.interfaces.config.IDhApiConfigValue;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeFogRenderEvent;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiCancelableEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiMutableFogRenderParam;
import com.vansqmod.VansqMod;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.Tags;

import java.awt.Color;

/**
 * Distant Horizons {@code Fog Min} reads as 1.0 below Y 60, regardless of biome
 * except oceans. The same Y blend also drops fog color Value to 20% (keeps hue/saturation).
 * Ocean biomes stay undarkened until Y 48, then fade to full cave fog by Y 0.
 * Sandstorms in Lost Caves leave fog color at default so YUNG's storm tint shows.
 * The saved DH config is not rewritten; camera position is used so spectator works.
 */
@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class LostCavesDhFogClient {

    /** Full cave fog at or below this camera Y. */
    private static final double FULL_FOG_Y = 52.0D;
    /** User Fog Min restored at or above this camera Y. */
    private static final double CLEAR_Y = 60.0D;
    /** Ocean biomes: full cave fog at or below this camera Y. */
    private static final double OCEAN_FULL_FOG_Y = 0.0D;
    /** Ocean biomes: no cave fog darkening at or above this camera Y. */
    private static final double OCEAN_CLEAR_Y = 48.0D;
    private static final float CAVE_FOG_MIN = 1.0F;
    /** HSV Value multiplier at full cave fog (80% darker). */
    private static final float CAVE_FOG_VALUE_SCALE = 0.2F;
    private static final float SMOOTH_RATE = 10.0F;
    private static final float MIX_EPSILON = 0.004F;

    private static volatile boolean overrideActive;
    private static volatile float overrideValue = CAVE_FOG_MIN;

    private static float smoothedMix;
    private static long lastNanos;
    private static boolean lastLogged;
    private static boolean fogColorEventBound;

    private LostCavesDhFogClient() {
    }

    public static boolean shouldOverrideFogMin() {
        return overrideActive;
    }

    public static float fogMinOverride() {
        return overrideValue;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) {
            if (overrideActive || smoothedMix > MIX_EPSILON) {
                clearAll();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) {
            return;
        }
        refresh(frameDeltaSeconds());
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        float scale = fogColorValueScale();
        if (scale >= 0.999F) {
            return;
        }
        event.setRed(event.getRed() * scale);
        event.setGreen(event.getGreen() * scale);
        event.setBlue(event.getBlue() * scale);
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clearAll();
    }

    private static float fogColorValueScale() {
        if (smoothedMix <= MIX_EPSILON || SandstormVanillaFogClient.shouldAllowVanillaFog()) {
            return 1.0F;
        }
        return Mth.lerp(smoothedMix, 1.0F, CAVE_FOG_VALUE_SCALE);
    }

    private static float frameDeltaSeconds() {
        long now = System.nanoTime();
        float dt = 1.0F / 60.0F;
        if (lastNanos != 0L) {
            dt = (now - lastNanos) / 1_000_000_000.0F;
        }
        lastNanos = now;
        return Mth.clamp(dt, 0.0F, 0.1F);
    }

    private static void refresh(float dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) {
            if (overrideActive || smoothedMix > MIX_EPSILON) {
                clearAll();
            }
            return;
        }
        if (!ModList.get().isLoaded("distanthorizons")) {
            if (overrideActive || smoothedMix > MIX_EPSILON) {
                clearAll();
            }
            return;
        }

        try {
            ensureFogColorEventBound();
            update(mc, mc.player, dt);
        } catch (Throwable t) {
            VansqMod.LOGGER.debug("DH underground fog tick failed", t);
        }
    }

    private static void clearAll() {
        smoothedMix = 0.0F;
        lastNanos = 0L;
        overrideActive = false;
        logChange(false);
    }

    private static void update(Minecraft mc, LocalPlayer player, float dt) {
        Vec3 cameraPos = cameraPos(mc, player);
        float targetMix = yBlend(cameraPos.y, isOceanBiome(mc, cameraPos));
        float alpha = 1.0F - (float) Math.exp(-SMOOTH_RATE * dt);
        smoothedMix += (targetMix - smoothedMix) * alpha;

        if (smoothedMix <= MIX_EPSILON) {
            smoothedMix = 0.0F;
            overrideActive = false;
            logChange(false);
            return;
        }

        float userMin = userFogMin();
        overrideValue = Mth.lerp(smoothedMix, userMin, CAVE_FOG_MIN);
        overrideActive = true;
        logChange(true);
    }

    private static float yBlend(double cameraY, boolean ocean) {
        double fullFogY = ocean ? OCEAN_FULL_FOG_Y : FULL_FOG_Y;
        double clearY = ocean ? OCEAN_CLEAR_Y : CLEAR_Y;
        if (cameraY <= fullFogY) {
            return 1.0F;
        }
        if (cameraY >= clearY) {
            return 0.0F;
        }
        float t = (float) ((cameraY - fullFogY) / (clearY - fullFogY));
        t = Mth.clamp(t, 0.0F, 1.0F);
        t = t * t * (3.0F - 2.0F * t);
        return 1.0F - t;
    }

    private static boolean isOceanBiome(Minecraft mc, Vec3 pos) {
        if (mc.level == null) {
            return false;
        }
        Holder<Biome> biome = mc.level.getBiome(BlockPos.containing(pos));
        return biome.is(BiomeTags.IS_OCEAN) || biome.is(Tags.Biomes.IS_OCEAN);
    }

    private static float userFogMin() {
        try {
            if (DhApi.Delayed.configs == null) {
                return 0.0F;
            }
            IDhApiConfigValue<Float> fogMin =
                    DhApi.Delayed.configs.graphics().fog().farFog().farFogMinThickness();
            if (fogMin == null) {
                return 0.0F;
            }
            Float trueValue = fogMin.getTrueValue();
            return trueValue != null ? trueValue : 0.0F;
        } catch (Throwable ignored) {
            return 0.0F;
        }
    }

    private static Vec3 cameraPos(Minecraft mc, LocalPlayer player) {
        try {
            Camera camera = mc.gameRenderer.getMainCamera();
            Vec3 pos = camera.getPosition();
            if (pos != null) {
                return pos;
            }
        } catch (Throwable ignored) {
            // Camera can be unset during dimension changes.
        }
        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity != null) {
            return cameraEntity.position();
        }
        return player.position();
    }

    private static void ensureFogColorEventBound() {
        if (fogColorEventBound) {
            return;
        }
        DhApi.events.bind(DhApiBeforeFogRenderEvent.class, new CaveDhFogColorEvent());
        fogColorEventBound = true;
    }

    /**
     * Scaling RGB by {@code k} is HSV Value {@code * k} (H and S unchanged).
     */
    private static Color scaleValue(Color color, float scale) {
        if (scale >= 0.999F) {
            return color;
        }
        int r = Mth.clamp(Math.round(color.getRed() * scale), 0, 255);
        int g = Mth.clamp(Math.round(color.getGreen() * scale), 0, 255);
        int b = Mth.clamp(Math.round(color.getBlue() * scale), 0, 255);
        if (r == color.getRed() && g == color.getGreen() && b == color.getBlue()) {
            return color;
        }
        return new Color(r, g, b, color.getAlpha());
    }

    private static boolean usesSkyFogColor() {
        try {
            if (DhApi.Delayed.configs == null) {
                return false;
            }
            IDhApiConfigValue<EDhApiFogColorMode> color =
                    DhApi.Delayed.configs.graphics().fog().color();
            return color != null && color.getTrueValue() == EDhApiFogColorMode.USE_SKY_COLOR;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void logChange(boolean active) {
        if (active == lastLogged) {
            return;
        }
        lastLogged = active;
        VansqMod.LOGGER.info(
                "Distant Horizons Fog Min override below Y {}: {}",
                (int) CLEAR_Y,
                active ? "ON" : "OFF"
        );
    }

    /**
     * DH default fog color is world fog (already darkened by {@link ViewportEvent.ComputeFogColor}).
     * Sky-color mode samples the sky instead, so darken it here with the same Y blend.
     */
    private static final class CaveDhFogColorEvent extends DhApiBeforeFogRenderEvent {
        @Override
        public void beforeRender(DhApiCancelableEventParam<EventParam> event) {
            float scale = fogColorValueScale();
            if (scale >= 0.999F || !usesSkyFogColor()) {
                return;
            }
            EventParam param = event.value;
            if (param == null) {
                return;
            }
            DhApiMutableFogRenderParam fog = param.getFogRenderParam();
            Color current = fog.getFogColor();
            if (current == null) {
                return;
            }
            fog.setFogColor(scaleValue(current, scale));
        }
    }
}
