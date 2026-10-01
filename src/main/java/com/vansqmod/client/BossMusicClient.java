package com.vansqmod.client;

import com.mojang.blaze3d.audio.Channel;
import com.vansqmod.VansqMod;
import com.vansqmod.boss.BossMusicRegistry;
import com.vansqmod.boss.BossMusicSpec;
import com.vansqmod.mixin.client.SoundEngineAccessor;
import com.vansqmod.mixin.client.SoundManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.util.UUID;

/**
 * Client-driven boss music. Presence of a tracked, fight-active boss starts
 * playback. Wither intro follows synced invulnerable ticks (skipped if you
 * join after spawn). Other intro bosses play intro whenever this client starts
 * the track, including joining an in-progress fight, then loop. Death outros
 * replace the loop and play through to the end.
 *
 * <p>Playback is tied to render frames and wall-clock audio duration, not
 * {@link Minecraft#isPaused()} or server ticks, so Multiplayer Server Pause
 * cannot freeze it the way tickable world SFX freeze.
 */
@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class BossMusicClient {

    /** Start the next track this far before the current file ends so stream open is hidden. */
    private static final long HANDOFF_OVERLAP_NANOS = 150_000_000L;
    private static final long HANDOFF_GIVE_UP_NANOS = 2_000_000_000L;
    private static final long MISSING_BOSS_GRACE_NANOS = 250_000_000L;
    /** Matches Reactive Music {@code PlayerThread.QUIET_VOLUME_PERCENTAGE}. */
    private static final float PAUSE_QUIET_VOLUME = 0.7F;
    /** RM lerps 0.02 per client tick (20 Hz). */
    private static final float PAUSE_QUIET_RATE_PER_SEC = 0.02F * 20.0F;

    private static UUID bossId;
    private static ResourceLocation loopLoc;
    private static ResourceLocation outroLoc;
    private static BossMusicSoundInstance playing;
    private static BossMusicSoundInstance incoming;
    private static boolean intro;
    private static boolean playingOutro;
    private static boolean stopping;
    private static int lastFadeOutTicks;
    private static boolean entityStateIntroPlaying;
    private static boolean introHeard;
    private static boolean outroHeard;
    private static boolean incomingHeard;
    private static long introEndsAtNanos;
    private static long outroEndsAtNanos;
    private static long incomingSinceNanos;
    private static long missingBossSinceNanos;
    private static float pauseQuiet = 1.0F;
    private static long lastQuietNanos;

    private BossMusicClient() {
    }

    /**
     * True while a boss track should hold Reactive Music down: a fight is in
     * range, or a death outro is still playing. The fade-out after leaving
     * the fight does not hold, so Reactive Music can come back.
     */
    public static boolean holdsReactiveMusic() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !mc.player.isAlive()) {
            return false;
        }
        if (playingOutro) {
            return true;
        }
        return findTrackedBoss(mc) != null;
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            stopImmediate();
            return;
        }

        tickPauseQuiet(mc);
        keepChannelAlive(playing);
        keepChannelAlive(incoming);

        LivingEntity boss = mc.player.isAlive() ? findTrackedBoss(mc) : null;
        if (boss != null) {
            missingBossSinceNanos = 0L;
            BossMusicSpec spec = BossMusicRegistry.get(boss);
            if (spec == null) {
                requestStop();
            } else {
                ensurePlaying(boss, spec);
            }
        } else if (playingOutro) {
            missingBossSinceNanos = 0L;
            // Keep the death track running after the entity is gone.
        } else if (shouldStartDeathOutro(mc)) {
            missingBossSinceNanos = 0L;
            startOutro();
        } else if (playing != null || incoming != null) {
            long now = System.nanoTime();
            if (missingBossSinceNanos == 0L) {
                missingBossSinceNanos = now;
            }
            if (now - missingBossSinceNanos >= MISSING_BOSS_GRACE_NANOS) {
                requestStop();
            }
        }
        tickHandoff(mc);
        tickPlayback(mc, boss);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        stopImmediate();
    }

    private static LivingEntity findTrackedBoss(Minecraft mc) {
        LivingEntity fallback = null;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
                continue;
            }
            BossMusicSpec spec = BossMusicRegistry.get(living);
            if (spec == null || !spec.isFightActive(living)) {
                continue;
            }
            if (bossId != null && bossId.equals(living.getUUID())) {
                return living;
            }
            if (fallback == null) {
                fallback = living;
            }
        }
        return fallback;
    }

    private static boolean shouldStartDeathOutro(Minecraft mc) {
        if (outroLoc == null || (playing == null && incoming == null)) {
            return false;
        }
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living) || living.isAlive()) {
                continue;
            }
            BossMusicSpec spec = BossMusicRegistry.get(living);
            if (spec == null || !spec.hasOutro()) {
                continue;
            }
            if (bossId != null && bossId.equals(living.getUUID())) {
                return true;
            }
            if (spec.outro().equals(outroLoc)) {
                return true;
            }
        }
        return false;
    }

    private static void ensurePlaying(LivingEntity living, BossMusicSpec spec) {
        UUID id = living.getUUID();
        boolean wantIntro = spec.intro() != null && (!spec.entityStateIntro() || spec.shouldPlayIntro(living));

        if (playingOutro) {
            stopImmediate();
        }

        if (stopping && playing != null && tryResume(id, playing.getLocation(), spec.lateJoinFadeInTicks())) {
            intro = spec.intro() != null && spec.intro().equals(playing.getLocation());
            remember(living, spec);
            return;
        }

        if (bossId != null && bossId.equals(id) && (playing != null || incoming != null) && !stopping) {
            if (spec.entityStateIntro() && intro && !wantIntro) {
                queueLoop(spec.loop(), 1.0F, 0);
            }
            return;
        }

        if (!intro && !playingOutro && playing != null && incoming == null && !stopping
                && loopLoc != null && loopLoc.equals(spec.loop())) {
            remember(living, spec);
            return;
        }

        if (wantIntro) {
            startIntro(living, spec);
        } else {
            startLoop(living, spec);
        }
    }

    private static void startIntro(LivingEntity living, BossMusicSpec spec) {
        stopImmediate();
        remember(living, spec);
        intro = true;
        stopping = false;
        entityStateIntroPlaying = spec.entityStateIntro();
        introHeard = false;
        playing = play(spec.intro(), false, 1.0F);
        if (playing == null) {
            startLoop(living, spec);
            return;
        }
        scheduleEnd(playing, true);
    }

    private static void startLoop(LivingEntity living, BossMusicSpec spec) {
        stopImmediate();
        remember(living, spec);
        stopping = false;
        float startVolume = spec.lateJoinFadeInTicks() > 0 ? 0.0F : 1.0F;
        queueLoop(spec.loop(), startVolume, spec.lateJoinFadeInTicks());
    }

    private static void startOutro() {
        if (outroLoc == null) {
            requestStop();
            return;
        }
        intro = false;
        stopping = false;
        playingOutro = true;
        outroHeard = false;
        entityStateIntroPlaying = false;
        introHeard = false;
        introEndsAtNanos = Long.MAX_VALUE;
        queueReplacement(outroLoc, false, 1.0F, 0, true);
    }

    private static void remember(LivingEntity living, BossMusicSpec spec) {
        bossId = living.getUUID();
        loopLoc = spec.loop();
        outroLoc = spec.outro();
        lastFadeOutTicks = spec.fadeOutTicks();
    }

    private static void requestStop() {
        if (playingOutro) {
            return;
        }
        if (incoming != null) {
            Minecraft.getInstance().getSoundManager().stop(incoming);
            incoming = null;
            incomingHeard = false;
        }
        if (playing == null || stopping) {
            return;
        }
        stopping = true;
        playing.fadeTo(0.0F, Math.max(1, lastFadeOutTicks), true);
    }

    /**
     * If this boss track is still fading out, reverse the fade instead of
     * starting a new instance (which would restart the loop).
     */
    private static boolean tryResume(UUID entityId, ResourceLocation wanted, int fadeInTicks) {
        if (!stopping || playing == null || wanted == null) {
            return false;
        }
        if (bossId == null || !bossId.equals(entityId) || playing.isStopped()) {
            return false;
        }
        if (!wanted.equals(playing.getLocation())) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!mc.getSoundManager().isActive(playing)) {
            return false;
        }
        stopping = false;
        float volume = Math.max(0.0F, playing.getVolume());
        int fullTicks = lastFadeOutTicks > 0 ? lastFadeOutTicks : Math.max(1, fadeInTicks);
        int ticks = Math.max(1, Math.round(fullTicks * (1.0F - volume)));
        playing.fadeTo(1.0F, ticks, false);
        return true;
    }

    private static void tickHandoff(Minecraft mc) {
        if (incoming == null) {
            return;
        }
        SoundManager manager = mc.getSoundManager();
        long now = System.nanoTime();
        if (manager.isActive(incoming) && !incoming.isStopped()) {
            incomingHeard = true;
        }
        boolean ready = incomingHeard || now - incomingSinceNanos >= HANDOFF_GIVE_UP_NANOS;
        if (!ready) {
            return;
        }
        if (playing != null && playing != incoming) {
            manager.stop(playing);
        }
        playing = incoming;
        incoming = null;
        incomingHeard = false;
        stopping = false;
        if (playingOutro) {
            outroHeard = manager.isActive(playing);
            scheduleEnd(playing, false);
        } else {
            intro = false;
            entityStateIntroPlaying = false;
            introHeard = false;
            introEndsAtNanos = Long.MAX_VALUE;
        }
    }

    private static void tickPlayback(Minecraft mc, LivingEntity boss) {
        SoundManager manager = mc.getSoundManager();
        if (playingOutro) {
            markHeard(manager);
            if (trackFinished(manager, outroHeard, outroEndsAtNanos)) {
                clear();
            }
            return;
        }
        if (stopping) {
            boolean silent = playing == null
                    || playing.isStopped()
                    || (playing.getVolume() <= 0.0F && !manager.isActive(playing));
            if (silent) {
                if (playing != null) {
                    manager.stop(playing);
                }
                clear();
            }
            return;
        }
        if (intro) {
            markHeard(manager);
            if (shouldAdvanceIntro(mc, boss, manager)) {
                queueLoop(loopLoc, 1.0F, 0);
            }
            return;
        }
        if (playing == null && incoming == null && loopLoc != null) {
            queueLoop(loopLoc, 1.0F, 0);
            return;
        }
        if (incoming != null || playing == null) {
            return;
        }
        if (manager.isActive(playing) && !playing.isStopped()) {
            introHeard = true;
            return;
        }
        if (loopLoc != null) {
            queueLoop(loopLoc, 1.0F, 0);
        }
    }

    private static boolean shouldAdvanceIntro(Minecraft mc, LivingEntity boss, SoundManager manager) {
        if (incoming != null || loopLoc == null) {
            return false;
        }
        if (System.nanoTime() >= introEndsAtNanos) {
            return true;
        }
        if (introHeard && playing != null && (playing.isStopped() || !manager.isActive(playing))) {
            return true;
        }
        if (entityStateIntroPlaying && boss != null) {
            BossMusicSpec spec = BossMusicRegistry.get(boss);
            return spec != null && spec.entityStateIntro() && !spec.shouldPlayIntro(boss);
        }
        return false;
    }

    private static void markHeard(SoundManager manager) {
        if (playing == null) {
            return;
        }
        if (manager.isActive(playing) && !playing.isStopped()) {
            if (playingOutro) {
                outroHeard = true;
            } else if (intro) {
                introHeard = true;
            }
        }
    }

    private static boolean trackFinished(SoundManager manager, boolean heard, long endsAtNanos) {
        if (incoming != null) {
            return false;
        }
        if (endsAtNanos != Long.MAX_VALUE && System.nanoTime() >= endsAtNanos) {
            return heard || playing == null || playing.isStopped() || !manager.isActive(playing);
        }
        return heard && (playing == null || playing.isStopped() || !manager.isActive(playing));
    }

    private static void queueLoop(ResourceLocation location, float startVolume, int fadeTicks) {
        queueReplacement(location, true, startVolume, fadeTicks, false);
    }

    private static void queueReplacement(
            ResourceLocation location,
            boolean looping,
            float startVolume,
            int fadeTicks,
            boolean outro
    ) {
        if (location == null) {
            return;
        }
        if (incoming != null && location.equals(incoming.getLocation())) {
            return;
        }
        if (incoming == null && playing != null && looping && !intro && !playingOutro
                && location.equals(playing.getLocation()) && !playing.isStopped()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getSoundManager().isActive(playing)) {
                return;
            }
        }
        if (incoming != null) {
            Minecraft.getInstance().getSoundManager().stop(incoming);
        }
        playingOutro = outro;
        incomingHeard = false;
        incomingSinceNanos = System.nanoTime();
        incoming = play(location, looping, startVolume);
        if (incoming != null && fadeTicks > 0 && startVolume < 1.0F) {
            incoming.fadeTo(1.0F, fadeTicks, false);
        }
        if (incoming == null && playing == null) {
            if (outro) {
                clear();
            }
        }
    }

    private static void scheduleEnd(BossMusicSoundInstance instance, boolean forIntro) {
        long duration = BossMusicOggDurations.nanos(instance);
        if (duration <= 0L) {
            if (forIntro) {
                introEndsAtNanos = Long.MAX_VALUE;
            } else {
                outroEndsAtNanos = Long.MAX_VALUE;
            }
            return;
        }
        long overlap = Math.min(HANDOFF_OVERLAP_NANOS, duration / 4L);
        long at = System.nanoTime() + duration - overlap;
        if (forIntro) {
            introEndsAtNanos = at;
        } else {
            outroEndsAtNanos = System.nanoTime() + duration;
        }
    }

    private static BossMusicSoundInstance play(ResourceLocation location, boolean looping, float startVolume) {
        if (location == null) {
            return null;
        }
        SoundEvent event = BuiltInRegistries.SOUND_EVENT.get(location);
        if (event == null) {
            VansqMod.LOGGER.warn("Missing boss music sound {}", location);
            return null;
        }
        BossMusicSoundInstance instance = new BossMusicSoundInstance(event, looping, startVolume);
        Minecraft.getInstance().getSoundManager().play(instance);
        ReactiveMusicMuteCompat.trackPlayingSound(instance, true);
        return instance;
    }

    private static void keepChannelAlive(BossMusicSoundInstance sound) {
        if (sound == null) {
            return;
        }
        sound.tickFade(System.nanoTime());
        Minecraft mc = Minecraft.getInstance();
        SoundEngine engine = ((SoundManagerAccessor) mc.getSoundManager()).vansqmod$soundEngine();
        if (engine == null) {
            return;
        }
        SoundEngineAccessor access = (SoundEngineAccessor) engine;
        ChannelAccess.ChannelHandle handle = access.vansqmod$instanceToChannel().get(sound);
        if (handle == null) {
            return;
        }
        float volume = access.vansqmod$calculateVolume(sound) * pauseQuiet;
        handle.execute((Channel channel) -> {
            channel.unpause();
            channel.updateStream();
            channel.setVolume(volume);
        });
    }

    /**
     * Reactive Music ducks to 70% while {@link Minecraft#isPaused()} unless the
     * sound options screen is open. Same target and 0.75s-ish lerp for boss tracks.
     */
    private static void tickPauseQuiet(Minecraft mc) {
        float target = shouldQuietForPause(mc) ? PAUSE_QUIET_VOLUME : 1.0F;
        long now = System.nanoTime();
        if (lastQuietNanos == 0L) {
            lastQuietNanos = now;
        }
        float dt = Mth.clamp((now - lastQuietNanos) / 1_000_000_000.0F, 0.0F, 0.25F);
        lastQuietNanos = now;
        pauseQuiet = Mth.approach(pauseQuiet, target, PAUSE_QUIET_RATE_PER_SEC * dt);
    }

    private static boolean shouldQuietForPause(Minecraft mc) {
        if (mc.level == null || !mc.isPaused()) {
            return false;
        }
        if (mc.screen instanceof SoundOptionsScreen) {
            return false;
        }
        if (mc.screen != null && mc.screen.getTitle().getContents() instanceof TranslatableContents contents) {
            return !"options.sounds.title".equals(contents.getKey());
        }
        return true;
    }

    private static void stopImmediate() {
        Minecraft mc = Minecraft.getInstance();
        if (incoming != null) {
            incoming.fadeTo(0.0F, 1, true);
            mc.getSoundManager().stop(incoming);
        }
        if (playing != null) {
            playing.fadeTo(0.0F, 1, true);
            mc.getSoundManager().stop(playing);
        }
        clear();
    }

    private static void clear() {
        playing = null;
        incoming = null;
        bossId = null;
        loopLoc = null;
        outroLoc = null;
        intro = false;
        playingOutro = false;
        stopping = false;
        lastFadeOutTicks = 0;
        entityStateIntroPlaying = false;
        introHeard = false;
        outroHeard = false;
        incomingHeard = false;
        introEndsAtNanos = Long.MAX_VALUE;
        outroEndsAtNanos = Long.MAX_VALUE;
        incomingSinceNanos = 0L;
        missingBossSinceNanos = 0L;
        lastQuietNanos = 0L;
    }
}
