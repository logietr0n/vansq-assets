package com.vansqmod.client;

import com.vansqmod.VansqMod;
import com.vansqmod.network.SandstormVanillaFogPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.lang.reflect.Method;

/**
 * Keeps Distant Horizons from suppressing vanilla fog while Lost Caves sandstorms
 * are active or imminent — including when the player walks in after the storm started.
 * <p>
 * Fog work is gated to in-world play only so title-screen / FancyMenu load is never touched.
 * {@link #shouldAllowVanillaFog()} is a volatile flag read from DH's fog path (must stay cheap).
 */
@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class SandstormVanillaFogClient {

    private static final ResourceKey<Biome> LOST_CAVES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "lost_caves")
    );

    private static final String SANDSTORM_PROVIDER =
            "com.yungnickyoung.minecraft.yungscavebiomes.client.render.sandstorm.ISandstormClientDataProvider";

    /** Keep fog on this long after conditions go false (biome edge / sync blips). */
    private static final int CLEAR_HYSTERESIS_TICKS = 40;

    /** Server warning / active flag from {@link SandstormVanillaFogPayload}. */
    private static boolean serverRequested;
    /** True once fog has been turned on; hysteresis only applies while latched. */
    private static boolean latchedOn;
    private static int clearTicks;
    private static boolean lastLoggedWant;

    /**
     * Read by DH fog mixin on the render thread. Updated only from client tick / network /
     * logout — never does Minecraft lookups itself.
     */
    private static volatile boolean allowVanillaFog;

    private static boolean reflectionResolved;
    private static Class<?> providerClass;
    private static Method getSandstormClientData;
    private static Method isSandstormActive;

    private SandstormVanillaFogClient() {
    }

    /**
     * Used by DH mixins every fog frame — must stay cheap, side-effect free, and never throw.
     */
    public static boolean shouldAllowVanillaFog() {
        return allowVanillaFog;
    }

    public static void handle(SandstormVanillaFogPayload payload) {
        serverRequested = payload.enable();
        // Packet can arrive while still loading; only apply if we're already in a world.
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.level != null && mc.player != null) {
            updateOverride();
        } else if (!payload.enable()) {
            clearAll();
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) {
            // Title screen / menus: do not touch DH APIs or sandstorm reflection.
            if (allowVanillaFog || latchedOn || serverRequested) {
                clearAll();
            }
            return;
        }

        try {
            updateOverride();
            DhVanillaFogOverride.tick();
        } catch (Throwable t) {
            VansqMod.LOGGER.debug("Sandstorm fog tick failed", t);
        }
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clearAll();
    }

    private static void clearAll() {
        serverRequested = false;
        latchedOn = false;
        clearTicks = 0;
        allowVanillaFog = false;
        logWantChange(false);
        try {
            DhVanillaFogOverride.setNeeded(false);
        } catch (Throwable ignored) {
            // Title-screen / early shutdown — leave DH alone if API isn't ready.
        }
    }

    private static void updateOverride() {
        boolean want = serverRequested || isLocalSandstormFogNeeded();

        if (want) {
            clearTicks = 0;
            latchedOn = true;
            allowVanillaFog = true;
            logWantChange(true);
            DhVanillaFogOverride.setNeeded(true);
            return;
        }

        // Only delay turning off after we have actually been on — never delay turning on.
        if (latchedOn && clearTicks < CLEAR_HYSTERESIS_TICKS) {
            clearTicks++;
            allowVanillaFog = true;
            DhVanillaFogOverride.setNeeded(true);
            return;
        }

        latchedOn = false;
        clearTicks = 0;
        allowVanillaFog = false;
        logWantChange(false);
        DhVanillaFogOverride.setNeeded(false);
    }

    private static void logWantChange(boolean want) {
        if (want == lastLoggedWant) {
            return;
        }
        lastLoggedWant = want;
        VansqMod.LOGGER.info("Lost Caves sandstorm vanilla fog override: {}", want ? "ON" : "OFF");
    }

    /**
     * True when the local client is in Lost Caves during an active sandstorm.
     * Uses the YCB provider interface so mid-storm entry works without waiting on the server packet.
     */
    private static boolean isLocalSandstormFogNeeded() {
        try {
            if (!ModList.get().isLoaded("yungscavebiomes")) {
                return false;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc == null) {
                return false;
            }
            LocalPlayer player = mc.player;
            Level level = mc.level;
            if (player == null || level == null) {
                return false;
            }
            if (!level.getBiome(player.blockPosition()).is(LOST_CAVES)) {
                return false;
            }

            return isSandstormActive(level);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isSandstormActive(Level level) {
        try {
            if (!resolveReflection()) {
                return false;
            }
            if (!providerClass.isInstance(level)) {
                return false;
            }
            Object data = getSandstormClientData.invoke(level);
            if (data == null) {
                return false;
            }
            Object active = isSandstormActive.invoke(data);
            return Boolean.TRUE.equals(active);
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static boolean resolveReflection() {
        if (reflectionResolved) {
            return providerClass != null && getSandstormClientData != null && isSandstormActive != null;
        }
        reflectionResolved = true;
        try {
            providerClass = Class.forName(SANDSTORM_PROVIDER);
            getSandstormClientData = providerClass.getMethod("getSandstormClientData");
            isSandstormActive = getSandstormClientData.getReturnType().getMethod("isSandstormActive");
            return true;
        } catch (ReflectiveOperationException e) {
            VansqMod.LOGGER.warn("Could not bind YCB sandstorm client API", e);
            providerClass = null;
            getSandstormClientData = null;
            isSandstormActive = null;
            return false;
        }
    }
}
