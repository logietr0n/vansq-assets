package com.vansqmod.client;

/**
 * Boss fights duck Reactive Music over the same 7.5 seconds Reactive Music
 * uses for a fade. Leaving before that finishes brings the same song back.
 * Reaching silence stops the file, and the next song waits out the normal gap
 * once the boss track is gone.
 */
public final class BossMusicDuck {

    public static final int FADE_TICKS = 150;

    private static int ticks;
    private static boolean stoppedSong;
    private static boolean releaseDelay;
    private static boolean holding;
    private static boolean justReleased;

    private BossMusicDuck() {
    }

    public static int tick() {
        boolean hold = BossMusicClient.holdsReactiveMusic();
        justReleased = holding && !hold;
        holding = hold;
        if (hold) {
            ticks = Math.min(FADE_TICKS, ticks + 1);
        } else {
            ticks = Math.max(0, ticks - 1);
            if (stoppedSong) {
                stoppedSong = false;
                releaseDelay = true;
            }
        }
        return ticks;
    }

    public static boolean consumeJustReleased() {
        boolean released = justReleased;
        justReleased = false;
        return released;
    }

    /** True on the tick the boss fade first reaches silence. */
    public static boolean reachedFullFade() {
        return BossMusicClient.holdsReactiveMusic() && ticks >= FADE_TICKS && !stoppedSong;
    }

    public static void markSongStopped() {
        stoppedSong = true;
    }

    public static boolean consumeReleaseDelay() {
        boolean release = releaseDelay;
        releaseDelay = false;
        return release;
    }
}
