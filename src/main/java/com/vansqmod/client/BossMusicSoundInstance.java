package com.vansqmod.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * Relative, unattenuated boss track so volume follows fades instead of distance.
 * Fades use wall-clock time so Multiplayer Server Pause cannot stall them.
 */
public final class BossMusicSoundInstance extends AbstractTickableSoundInstance {

    private static final long NANOS_PER_TICK = 50_000_000L;

    private float fadeFrom;
    private float fadeTo;
    private long fadeStartNanos;
    private long fadeDurationNanos;
    private boolean fading;
    private boolean stopWhenSilent;

    public BossMusicSoundInstance(SoundEvent event, boolean looping, float startVolume) {
        super(event, SoundSource.MUSIC, RandomSource.create());
        this.looping = looping;
        this.delay = 0;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.relative = true;
        this.volume = startVolume;
        this.pitch = 1.0F;
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
    }

    public void fadeTo(float target, int ticks, boolean stopWhenSilent) {
        fadeTo(target, Math.max(1, ticks) * NANOS_PER_TICK, stopWhenSilent);
    }

    public void fadeTo(float target, long durationNanos, boolean stopWhenSilent) {
        this.fadeFrom = this.volume;
        this.fadeTo = target;
        this.fadeDurationNanos = Math.max(1L, durationNanos);
        this.fadeStartNanos = System.nanoTime();
        this.fading = true;
        this.stopWhenSilent = stopWhenSilent;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        tickFade(System.nanoTime());
    }

    public void tickFade(long now) {
        if (!fading) {
            return;
        }
        float t = Mth.clamp((now - fadeStartNanos) / (float) fadeDurationNanos, 0.0F, 1.0F);
        volume = Mth.lerp(t, fadeFrom, fadeTo);
        if (t < 1.0F) {
            return;
        }
        fading = false;
        volume = fadeTo;
        if (stopWhenSilent && fadeTo <= 0.0F) {
            stop();
        }
    }
}
