package com.vansqmod.boss;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Intro + looping boss track, evaluated on the client from the tracked entity.
 *
 * <p>{@link #entityStateIntro} (Wither): play intro while {@link #playIntro} is
 * true (invulnerability), then switch to the loop.
 *
 * <p>Otherwise if {@link #intro} is set, play it whenever this client starts the
 * fight track (including joining mid-fight), then the loop when that track finishes.
 *
 * <p>If {@link #outro} is set, death replaces the loop with that track and lets
 * it play through instead of fading out.
 */
public record BossMusicSpec(
        @Nullable ResourceLocation intro,
        ResourceLocation loop,
        @Nullable ResourceLocation outro,
        int lateJoinFadeInTicks,
        int fadeOutTicks,
        Predicate<LivingEntity> fightActive,
        Predicate<LivingEntity> playIntro,
        boolean entityStateIntro
) {
    public static BossMusicSpec untilEntityStateEnds(
            ResourceLocation intro,
            ResourceLocation loop,
            int lateJoinFadeInTicks,
            int fadeOutTicks,
            Predicate<LivingEntity> playIntro
    ) {
        return new BossMusicSpec(intro, loop, null, lateJoinFadeInTicks, fadeOutTicks, living -> true, playIntro, true);
    }

    public static BossMusicSpec onFightStart(
            ResourceLocation intro,
            ResourceLocation loop,
            int lateJoinFadeInTicks,
            int fadeOutTicks,
            Predicate<LivingEntity> fightActive
    ) {
        return new BossMusicSpec(intro, loop, null, lateJoinFadeInTicks, fadeOutTicks, fightActive, living -> false, false);
    }

    public static BossMusicSpec loopWithDeathOutro(
            ResourceLocation loop,
            ResourceLocation outro,
            int lateJoinFadeInTicks,
            int fadeOutTicks,
            Predicate<LivingEntity> fightActive
    ) {
        return new BossMusicSpec(null, loop, outro, lateJoinFadeInTicks, fadeOutTicks, fightActive, living -> false, false);
    }

    public boolean isFightActive(LivingEntity living) {
        return fightActive.test(living);
    }

    public boolean shouldPlayIntro(LivingEntity living) {
        return playIntro.test(living);
    }

    public boolean hasOutro() {
        return outro != null;
    }
}
