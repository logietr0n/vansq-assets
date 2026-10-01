package com.vansqmod.entity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Shared combat-fishing numbers for Putrid and Mob AI Tweaks fishermen.
 * Bobber pull/reel still uses MAIT's hook mixin for flight and sticking;
 * vansq only changes when the line comes back.
 */
public final class MobFishingRodAi {

    public static final double MIN_CAST_DISTANCE_SQR = 1.0D;
    public static final double MAX_CAST_DISTANCE_SQR = 169.0D;
    public static final int CAST_INTERVAL_TICKS = 60;
    public static final double CAST_SPEED = 2.0D;

    /** Empty hook is reeled in this many ticks after the cast. */
    public static final int MISS_REEL_TICKS = 10;
    /** Hooked target is yanked toward the caster after this many ticks. */
    public static final int HIT_REEL_TICKS = 30;
    public static final int REEL_SWING_LEAD_TICKS = 3;
    private static final double YANK_SCALE = 0.25D;
    private static final byte HOOK_PULL_EVENT = 31;

    private MobFishingRodAi() {
    }

    public static boolean holdsFishingRod(LivingEntity entity) {
        return entity.getMainHandItem().is(Items.FISHING_ROD)
                || entity.getOffhandItem().is(Items.FISHING_ROD);
    }

    public static boolean isMobCombatHook(FishingHook hook) {
        Entity owner = hook.getOwner();
        return owner instanceof LivingEntity living
                && !(owner instanceof Player)
                && holdsFishingRod(living);
    }

    /**
     * Reels a mob-owned hook on vansq's schedule. Returns true if the hook
     * was discarded this tick.
     */
    public static boolean tickReel(FishingHook hook) {
        if (hook.isRemoved() || !isMobCombatHook(hook)) {
            return false;
        }
        Entity owner = hook.getOwner();
        if (!(owner instanceof LivingEntity living)) {
            return false;
        }
        Entity hooked = hookedTarget(hook);
        int reelAt = hooked != null ? HIT_REEL_TICKS : MISS_REEL_TICKS;
        int age = hook.tickCount;
        if (age >= reelAt - REEL_SWING_LEAD_TICKS && age < reelAt && !living.onGround()) {
            living.swing(InteractionHand.MAIN_HAND);
        }
        if (age < reelAt) {
            return false;
        }
        if (hooked != null) {
            Vec3 yank = living.getEyePosition().subtract(hooked.position()).scale(YANK_SCALE);
            hooked.addDeltaMovement(yank);
            hook.level().broadcastEntityEvent(hook, HOOK_PULL_EVENT);
        }
        hook.discard();
        living.stopUsingItem();
        return true;
    }

    private static Entity hookedTarget(FishingHook hook) {
        Entity hooked = hook.getHookedIn();
        return hooked != null && hooked.isAlive() ? hooked : null;
    }
}
