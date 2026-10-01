package com.vansqmod.mixin.reactivemusic;

import com.vansqmod.client.BossMusicDuck;
import com.vansqmod.client.GoatManMusicSilence;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * Boss music and Goat Man stalk/chase duck Reactive Music. Boss music owns its
 * own fade: leaving the fight before silence brings the same song back, and
 * reaching silence stops that file. Boss sound instances are removed from
 * Reactive Music's tracker so a fading-out boss track does not keep the song down.
 */
@Mixin(targets = "circuitlord.reactivemusic.ReactiveMusic", remap = false)
public abstract class ReactiveMusicGoatManDuckMixin {

    private static final float DUCK_MAX = 150.0F;

    @Inject(method = "processTrackedSoundsMuteMusic", at = @At("HEAD"))
    private static void vansqmod$stripBossSounds(CallbackInfo ci) {
        stripBossSounds();
    }

    @Inject(method = "processTrackedSoundsMuteMusic", at = @At("RETURN"))
    private static void vansqmod$goatmanDuck(CallbackInfo ci) {
        int goat = GoatManMusicSilence.tickDuckRamp();
        int boss = BossMusicDuck.tick();
        try {
            Class<?> rm = Class.forName("circuitlord.reactivemusic.ReactiveMusic");
            Field ticks = rm.getDeclaredField("musicTrackedSoundsDuckTicks");
            ticks.setAccessible(true);
            int combined = Math.max(boss, Math.max(goat, ticks.getInt(null)));
            ticks.setInt(null, combined);
            Object thread = rm.getField("thread").get(null);
            if (thread != null) {
                Method setDuck = thread.getClass().getMethod("setMusicDiscDuckPercentage", float.class);
                setDuck.invoke(thread, 1.0F - combined / DUCK_MAX);
            }
            if (BossMusicDuck.reachedFullFade()) {
                Method reset = rm.getDeclaredMethod("resetPlayer");
                reset.setAccessible(true);
                reset.invoke(null);
                BossMusicDuck.markSongStopped();
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private static void stripBossSounds() {
        try {
            Class<?> rm = Class.forName("circuitlord.reactivemusic.ReactiveMusic");
            Field tracked = rm.getDeclaredField("trackedSoundsMuteMusic");
            tracked.setAccessible(true);
            Object raw = tracked.get(null);
            if (!(raw instanceof List<?> list) || list.isEmpty()) {
                return;
            }
            Field soundField = null;
            Iterator<?> iterator = list.iterator();
            while (iterator.hasNext()) {
                Object entry = iterator.next();
                if (soundField == null) {
                    soundField = entry.getClass().getDeclaredField("soundInstance");
                    soundField.setAccessible(true);
                }
                if (soundField.get(entry) instanceof SoundInstance sound
                        && sound.getLocation().getPath().contains("music.boss")) {
                    iterator.remove();
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
