package com.vansqmod.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Seamless Sleep defers {@code setDayTime} / {@code wakeUpAllPlayers} into a
 * multi-tick animation. Sleep Tight can wake players at skip-start (encounters)
 * and Seamless then cancels the skip because not enough people are still in bed.
 */
public final class SleepCompat {

    private static final String SEAMLESS_MOD = "seamlesssleep";
    private static final String ANIMATION_CLASS = "net.aqualoco.sec.SeamlessSleepCommon";
    private static final String STATE_FIELD = "OVERWORLD_SLEEP_ANIMATION";
    private static final String ENCOUNTER_CLASS = "net.mehvahdjukaar.sleep_tight.core.WakeUpEncounterHelper";

    private static final List<DeferredEncounter> DEFERRED_ENCOUNTERS = new ArrayList<>();
    private static final List<UUID> SEAMLESS_WAKE = new ArrayList<>();

    private static boolean animationResolved;
    private static Object animationState;
    private static Method isActive;

    private static boolean encounterResolved;
    private static Method tryPerformEncounter;
    private static boolean replayingEncounter;

    private SleepCompat() {
    }

    public static boolean seamlessLoaded() {
        return ModList.get().isLoaded(SEAMLESS_MOD);
    }

    public static boolean isSleepAnimationActive() {
        Object state = animationState();
        if (state == null || isActive == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(isActive.invoke(state));
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    public static void markSeamlessWake(ServerLevel level) {
        SEAMLESS_WAKE.clear();
        for (ServerPlayer player : level.players()) {
            if (player.isSleeping()) {
                SEAMLESS_WAKE.add(player.getUUID());
            }
        }
    }

    public static boolean shouldTreatAsActualSleep(Player player, PlayerWakeUpEvent event) {
        return player != null && SEAMLESS_WAKE.contains(player.getUUID());
    }

    public static void clearSeamlessWake(Player player) {
        if (player != null) {
            SEAMLESS_WAKE.remove(player.getUUID());
        }
    }

    public static boolean shouldDeferEncounters() {
        return !replayingEncounter
                && seamlessLoaded()
                && ModList.get().isLoaded("sleep_tight");
    }

    public static void deferEncounter(ServerPlayer player, BlockPos pos) {
        if (player == null || pos == null) {
            return;
        }
        DEFERRED_ENCOUNTERS.removeIf(pending -> pending.playerId.equals(player.getUUID()));
        DEFERRED_ENCOUNTERS.add(new DeferredEncounter(player.getUUID(), pos.immutable()));
    }

    public static void replayDeferredEncounter(ServerPlayer player) {
        if (player == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        Method method = encounterMethod();
        if (method == null) {
            return;
        }
        Iterator<DeferredEncounter> iterator = DEFERRED_ENCOUNTERS.iterator();
        while (iterator.hasNext()) {
            DeferredEncounter pending = iterator.next();
            if (!pending.playerId.equals(player.getUUID())) {
                continue;
            }
            iterator.remove();
            replayingEncounter = true;
            try {
                method.invoke(null, player, level, pending.pos);
            } catch (ReflectiveOperationException ignored) {
                return;
            } finally {
                replayingEncounter = false;
            }
            return;
        }
    }

    private static Object animationState() {
        if (animationResolved) {
            return animationState;
        }
        animationResolved = true;
        if (!seamlessLoaded()) {
            return null;
        }
        try {
            Class<?> common = Class.forName(ANIMATION_CLASS);
            Field field = common.getField(STATE_FIELD);
            animationState = field.get(null);
            isActive = animationState.getClass().getMethod("isActive");
        } catch (ReflectiveOperationException ignored) {
            animationState = null;
            isActive = null;
        }
        return animationState;
    }

    private static Method encounterMethod() {
        if (encounterResolved) {
            return tryPerformEncounter;
        }
        encounterResolved = true;
        try {
            Class<?> helper = Class.forName(ENCOUNTER_CLASS);
            tryPerformEncounter = helper.getMethod(
                    "tryPerformEncounter",
                    ServerPlayer.class,
                    ServerLevel.class,
                    BlockPos.class
            );
        } catch (ReflectiveOperationException ignored) {
            tryPerformEncounter = null;
        }
        return tryPerformEncounter;
    }

    private record DeferredEncounter(UUID playerId, BlockPos pos) {
    }
}
