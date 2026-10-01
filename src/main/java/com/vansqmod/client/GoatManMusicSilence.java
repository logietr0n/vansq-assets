package com.vansqmod.client;

import com.vansqmod.VansqMod;
import com.vansqmod.compat.GoatManDifficulty;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Goat Man stalk/chase should duck Reactive Music the same way boss tracks do,
 * without playing a replacement track.
 */
@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class GoatManMusicSilence {

    private static final int DUCK_TICKS = 150;
    private static final int MISSING_GRACE_TICKS = 100;
    private static final double NEAR_RANGE = 160.0;

    private static boolean wantDuck;
    private static int duckTicks;
    private static int missingTicks;

    private GoatManMusicSilence() {
    }

    public static void onMusicPacket(Object packet) {
        String type = musicType(packet);
        if (type == null) {
            return;
        }
        switch (type) {
            case "STALK_START", "CHASE_PLAY", "CHASE_LEGACY_PLAY" -> {
                wantDuck = true;
                missingTicks = 0;
            }
            case "STALK_STOP", "CHASE_STOP", "FORCE_STOP_ALL" -> wantDuck = false;
            default -> {
            }
        }
    }

    public static int tickDuckRamp() {
        if (shouldDuck()) {
            duckTicks = Math.min(DUCK_TICKS, duckTicks + 1);
        } else {
            duckTicks = Math.max(0, duckTicks - 1);
        }
        return duckTicks;
    }

    public static boolean shouldDuck() {
        Minecraft mc = Minecraft.getInstance();
        return wantDuck && mc.level != null && !GoatManDifficulty.isSuppressed(mc.level);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!wantDuck) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || GoatManDifficulty.isSuppressed(mc.level)) {
            wantDuck = false;
            return;
        }
        boolean near = false;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (GoatManDifficulty.isGoatMan(entity) && entity.isAlive()
                    && entity.distanceToSqr(mc.player) <= NEAR_RANGE * NEAR_RANGE) {
                near = true;
                break;
            }
        }
        if (near) {
            missingTicks = 0;
        } else if (++missingTicks > MISSING_GRACE_TICKS) {
            wantDuck = false;
        }
    }

    private static String musicType(Object packet) {
        if (packet == null) {
            return null;
        }
        try {
            Object type = packet.getClass().getMethod("musicType").invoke(packet);
            return type == null ? null : type.toString();
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
