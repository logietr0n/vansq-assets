package com.vansqmod.boss;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client map of vanilla boss-bar UUIDs to hex tints sent by {@link BossBarHandler}.
 */
public final class BossBarColorTracker {

    private static final Map<UUID, Integer> COLORS = new ConcurrentHashMap<>();

    private BossBarColorTracker() {
    }

    public static void set(UUID barId, int rgb) {
        COLORS.put(barId, rgb);
    }

    public static void clear(UUID barId) {
        COLORS.remove(barId);
    }

    public static @Nullable Integer get(UUID barId) {
        return COLORS.get(barId);
    }

    public static void reset() {
        COLORS.clear();
    }
}
