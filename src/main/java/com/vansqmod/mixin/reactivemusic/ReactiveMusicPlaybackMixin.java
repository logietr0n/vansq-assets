package com.vansqmod.mixin.reactivemusic;

import circuitlord.reactivemusic.PlayerThread;
import circuitlord.reactivemusic.entries.RMRuntimeEntry;
import com.vansqmod.client.BossMusicClient;
import com.vansqmod.client.BossMusicDuck;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

/**
 * Songs play out, then the pack's normal silence gap runs, unless an entry
 * with {@code forceStopMusicOnInvalid} fades them the way Reactive Music
 * already does. Changing dimension or leaving the main menu cuts the file
 * immediately, and the next song still waits out that gap.
 */
@Mixin(targets = "circuitlord.reactivemusic.ReactiveMusic", remap = false)
public abstract class ReactiveMusicPlaybackMixin {

    @Shadow
    static PlayerThread thread;

    @Shadow
    static String currentSong;

    @Shadow
    static boolean queuedToStopMusic;

    @Shadow
    static boolean queuedToPlayMusic;

    @Shadow
    static int waitForNewSongTicks;

    @Shadow
    static void resetPlayer() {
    }

    private static boolean sawDimension;
    private static String lastDimension;
    private static boolean suppressForceStart;

    @Inject(method = "newTick", at = @At("HEAD"))
    private static void vansqmod$cutOnDimensionOrMenu(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        String dimension = mc.level == null ? null : mc.level.dimension().location().toString();
        if (sawDimension && thread != null && !Objects.equals(dimension, lastDimension)) {
            resetPlayer();
            waitForNewSongTicks = 0;
            queuedToPlayMusic = false;
            queuedToStopMusic = false;
            if (dimension != null) {
                suppressForceStart = true;
            }
        }
        sawDimension = true;
        lastDimension = dimension;
    }

    @Inject(method = "processValidEvents", at = @At("RETURN"))
    private static void vansqmod$keepSilenceAfterCut(List<RMRuntimeEntry> current, List<RMRuntimeEntry> previous, CallbackInfo ci) {
        boolean bossRelease = BossMusicDuck.consumeReleaseDelay();
        boolean leftFight = BossMusicDuck.consumeJustReleased();
        boolean bossSilence = bossRelease || (leftFight && currentSong == null);
        if (suppressForceStart || bossSilence) {
            queuedToPlayMusic = false;
            waitForNewSongTicks = 0;
            suppressForceStart = false;
        }
    }

    /**
     * Entry changes no longer fade the current song on the switch timer.
     * {@code queuedToStopMusic} is set by {@code forceStopMusicOnInvalid}
     * (and {@code forceStopMusicOnValid}), which still fades as before.
     */
    @Inject(method = "tickFadeOut", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$onlyForceStopFades(CallbackInfo ci) {
        if (!queuedToStopMusic) {
            ci.cancel();
        }
    }

    @Inject(method = "changeCurrentSong", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$waitOutBoss(String song, RMRuntimeEntry entry, CallbackInfo ci) {
        if (BossMusicClient.holdsReactiveMusic()) {
            ci.cancel();
        }
    }
}
