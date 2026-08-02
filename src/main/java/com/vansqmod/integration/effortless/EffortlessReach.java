package com.vansqmod.integration.effortless;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves Effortless Building reach from the player's block interaction range, except in Creative
 * where the mod's configured max reach is kept.
 */
public final class EffortlessReach {

    private static final int DEFAULT_CONFIG_REACH = 128;

    private static final ThreadLocal<Object> TRACE_CONTEXT = new ThreadLocal<>();

    private EffortlessReach() {
    }

    public static void setTraceContext(Object context) {
        if (context != null) {
            TRACE_CONTEXT.set(context);
        }
    }

    public static void clearTraceContext() {
        TRACE_CONTEXT.remove();
    }

    public static int traceReachOrDefault(int fallback) {
        Object context = TRACE_CONTEXT.get();
        if (context != null) {
            return resolveReach(context);
        }
        return resolveReachFromLocalPlayer(fallback);
    }

    public static int resolveReach(Object context) {
        int configReach = readConfigMaxReach(context);
        Player player = resolveBuildingPlayer();
        if (player == null) {
            return configReach;
        }
        if (player.isCreative()) {
            return configReach;
        }
        return attributeReach(player);
    }

    private static int resolveReachFromLocalPlayer(int fallback) {
        Player player = resolveBuildingPlayer();
        if (player == null) {
            return fallback;
        }
        if (player.isCreative()) {
            return DEFAULT_CONFIG_REACH;
        }
        return attributeReach(player);
    }

    private static int attributeReach(Player player) {
        return Math.max(0, (int) Math.floor(player.blockInteractionRange()));
    }

    /**
     * Effortless {@code Context.id()} is a per-session random UUID, not the player id.
     * Tracing runs on the client, so the local player is the reliable source.
     */
    @Nullable
    private static Player resolveBuildingPlayer() {
        var client = Minecraft.getInstance();
        if (client.player != null) {
            return client.player;
        }

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }

        if (server.getPlayerCount() == 1) {
            return server.getPlayerList().getPlayers().getFirst();
        }

        return null;
    }

    private static int readConfigMaxReach(Object context) {
        try {
            Object configs = context.getClass().getMethod("configs").invoke(context);
            Object constraint = configs.getClass().getMethod("constraintConfig").invoke(configs);
            Object reach = constraint.getClass().getMethod("maxReachDistance").invoke(constraint);
            if (reach instanceof Integer integer) {
                return integer;
            }
        } catch (ReflectiveOperationException ignored) {
            // Fall through to default.
        }
        return DEFAULT_CONFIG_REACH;
    }
}
