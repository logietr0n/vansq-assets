package com.vansqmod.compat;

import dev.chybx.spideroverhaul.entity.OceanSpiderEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Underwater Spider Crab jumps: a low-gravity ballistic arc from the seafloor.
 * Height scales to the swimmer (capped at 24), and a player at/below a short hop
 * only gets that hop. Horizontal close is ~1.5s with in-air steering, so they
 * track the player without the old rocket dash. Land pounces stay with Spider
 * Overhaul's own goal.
 */
public final class OceanSpiderLeap {

    public static final int BOG_DURATION_TICKS = 40;
    public static final double FOLLOW_RANGE = 40.0D;
    /** Spider Overhaul base is 5; other spiders are 2. */
    public static final double ATTACK_DAMAGE = 2.0D;
    /** Same on land and seafloor; water no longer slows grounded crabs. */
    public static final double MOVEMENT_SPEED = 0.3D;
    /** Extra melee/leap hit distance on top of vanilla mob attack reach. */
    public static final double ATTACK_REACH_BONUS = 0.5D;
    /** Sink through the water column this much faster than vanilla fluid gravity. */
    public static final double SINK_GRAVITY_SCALE = 1.5D;

    /** Highest they can rise in one jump. */
    private static final double MAX_JUMP_HEIGHT = 24.0D;
    /** Hop used when the player is not above this. */
    private static final double NORMAL_JUMP_HEIGHT = 3.5D;
    private static final double MAX_HORIZONTAL = 16.0D;
    private static final double MIN_LEAP_DIST_SQ = 4.0D;
    private static final double HIT_RANGE = 1.8D + ATTACK_REACH_BONUS;
    private static final double HIT_RANGE_SQ = HIT_RANGE * HIT_RANGE;
    /** Blocks/tick^{2}. Air gravity is 0.08; this is a slow underwater float. */
    private static final double GRAVITY = 0.022D;
    /**
     * ~1.5s horizontal close. Typical 6–10 block pounces take about a second
     * and a half; max-range 16 takes a little over.
     */
    private static final double MAX_HORIZ_SPEED = 0.50D;
    /** Aim to cover the player's XZ in this many ticks at leap start. */
    private static final double TARGET_HORIZ_TICKS = 30.0D;
    private static final double TERMINAL_FALL = 0.16D;
    /** Blend toward the desired heading each tick (1 = snap). */
    private static final double STEER = 0.18D;
    private static final int LANDING_COOLDOWN_TICKS = 45;
    private static final int MAX_AIR_TICKS = 140;
    /** Idle hiss once per jump when this close to a player. */
    private static final double APPROACH_IDLE_RANGE = 8.0D;
    private static final float APPROACH_IDLE_VOLUME = 2.0F;

    private static final Map<OceanSpiderEntity, State> STATE = new WeakHashMap<>();

    private OceanSpiderLeap() {
    }

    public static boolean isOceanSpider(Entity entity) {
        return entity instanceof OceanSpiderEntity;
    }

    public static boolean isDescending(OceanSpiderEntity crab) {
        State state = STATE.get(crab);
        return state != null && state.airborne && crab.getDeltaMovement().y <= 0.0D;
    }

    public static boolean canLeapFromSeafloor(OceanSpiderEntity crab) {
        return crab.leapCooldown <= 0
                && !crab.isLeaping()
                && !isAirborne(crab)
                && isOnSeafloor(crab);
    }

    public static boolean canStartWaterLeap(OceanSpiderEntity crab) {
        LivingEntity target = crab.getTarget();
        if (target == null || !target.isAlive() || !crab.isInWater()) {
            return false;
        }
        if (!canLeapFromSeafloor(crab)) {
            return false;
        }
        double dx = target.getX() - crab.getX();
        double dy = target.getY() - crab.getY();
        double dz = target.getZ() - crab.getZ();
        if (dy > MAX_JUMP_HEIGHT + 2.0D) {
            return false;
        }
        if (dx * dx + dz * dz > MAX_HORIZONTAL * MAX_HORIZONTAL) {
            return false;
        }
        return crab.distanceToSqr(target) > MIN_LEAP_DIST_SQ;
    }

    public static void startWaterLeap(OceanSpiderEntity crab) {
        LivingEntity target = crab.getTarget();
        if (target == null) {
            return;
        }
        State state = state(crab);
        state.airborne = true;
        state.hitThisLeap = false;
        state.playedApproachIdle = false;
        state.airTicks = 0;

        Vec3 aim = aimPoint(target);
        double rise = aim.y - crab.getY();
        double jumpHeight;
        if (rise <= NORMAL_JUMP_HEIGHT) {
            jumpHeight = Mth.clamp(Math.max(rise, 1.25D), 1.25D, NORMAL_JUMP_HEIGHT);
        } else {
            jumpHeight = Mth.clamp(rise + 0.6D, NORMAL_JUMP_HEIGHT, MAX_JUMP_HEIGHT);
        }
        double vy = Math.sqrt(2.0D * GRAVITY * jumpHeight);

        double dx = aim.x - crab.getX();
        double dz = aim.z - crab.getZ();
        double[] horizVel = horizontalToward(dx, dz, TARGET_HORIZ_TICKS);
        crab.setDeltaMovement(horizVel[0], vy, horizVel[1]);
        crab.hasImpulse = true;
        crab.leapAnimTicks = 0;
        crab.leapGroundCheckDelay = 8;
        crab.setLeaping(true);
    }

    public static void tick(OceanSpiderEntity crab) {
        if (crab.leapCooldown > 0) {
            --crab.leapCooldown;
        }
        if (crab.leapGroundCheckDelay > 0) {
            --crab.leapGroundCheckDelay;
        }
        State state = state(crab);
        tickApproachIdle(crab);
        if (!state.airborne) {
            if (crab.isLeaping() && isOnSeafloor(crab) && crab.leapGroundCheckDelay <= 0) {
                crab.setLeaping(false);
            }
            return;
        }
        if (crab.leapAnimTicks < 40) {
            ++crab.leapAnimTicks;
        }
        ++state.airTicks;

        LivingEntity target = crab.getTarget();
        if (target != null && target.isAlive()) {
            crab.getLookControl().setLookAt(target, 30.0F, 70.0F);
            tryHit(crab, state, target);
            Vec3 vel = crab.getDeltaMovement();
            double dx = target.getX() - crab.getX();
            double dz = target.getZ() - crab.getZ();
            double remaining = Math.max(12.0D, TARGET_HORIZ_TICKS - state.airTicks);
            double[] desired = horizontalToward(dx, dz, remaining);
            crab.setDeltaMovement(
                    vel.x + (desired[0] - vel.x) * STEER,
                    vel.y,
                    vel.z + (desired[1] - vel.z) * STEER
            );
        }

        boolean leftWater = !crab.isInWater();
        boolean timedOut = state.airTicks >= MAX_AIR_TICKS;
        boolean ceiling = crab.verticalCollision && crab.getDeltaMovement().y > 0.0D;
        if (leftWater || timedOut || ceiling) {
            if (ceiling) {
                crab.setDeltaMovement(crab.getDeltaMovement().x, 0.0D, crab.getDeltaMovement().z);
            }
        }
        if ((isOnSeafloor(crab) && crab.leapGroundCheckDelay <= 0) || timedOut || leftWater) {
            land(crab, state);
        }
    }

    public static boolean handleTravel(OceanSpiderEntity crab) {
        if (!crab.isInWater()) {
            return false;
        }
        State state = STATE.get(crab);
        if (state == null || !state.airborne) {
            return false;
        }
        Vec3 vel = crab.getDeltaMovement();
        crab.move(MoverType.SELF, vel);
        double vy = vel.y - GRAVITY;
        if (vy < -TERMINAL_FALL) {
            vy = -TERMINAL_FALL;
        }
        crab.setDeltaMovement(vel.x, vy, vel.z);
        crab.resetFallDistance();
        crab.hasImpulse = true;
        return true;
    }

    private static boolean isAirborne(OceanSpiderEntity crab) {
        State state = STATE.get(crab);
        return state != null && state.airborne;
    }

    private static void land(OceanSpiderEntity crab, State state) {
        state.airborne = false;
        state.airTicks = 0;
        state.playedApproachIdle = false;
        crab.setLeaping(false);
        crab.leapCooldown = LANDING_COOLDOWN_TICKS;
        crab.setDeltaMovement(crab.getDeltaMovement().multiply(0.25D, 0.0D, 0.25D));
    }

    private static void tryHit(OceanSpiderEntity crab, State state, LivingEntity target) {
        if (state.hitThisLeap || crab.distanceToSqr(target) > HIT_RANGE_SQ) {
            return;
        }
        if (crab.doHurtTarget(target)) {
            state.hitThisLeap = true;
        }
    }

    private static boolean isOnSeafloor(OceanSpiderEntity crab) {
        if (!crab.isInWater()) {
            return crab.onGround();
        }
        if (crab.onGround() && crab.leapGroundCheckDelay <= 0) {
            return true;
        }
        BlockPos floor = crab.blockPosition().below();
        if (crab.level().getFluidState(floor).is(FluidTags.WATER)) {
            return false;
        }
        BlockState floorState = crab.level().getBlockState(floor);
        return floorState.isFaceSturdy(crab.level(), floor, Direction.UP);
    }

    private static double[] horizontalToward(double dx, double dz, double ticks) {
        double dist = Math.hypot(dx, dz);
        if (dist < 0.05D) {
            return new double[] {0.0D, 0.0D};
        }
        double speed = Math.min(MAX_HORIZ_SPEED, dist / Math.max(1.0D, ticks));
        return new double[] {(dx / dist) * speed, (dz / dist) * speed};
    }

    public static void tickApproachIdle(OceanSpiderEntity crab) {
        State state = state(crab);
        if (state.airborne || crab.isLeaping()) {
            tryPlayApproachIdle(crab, state);
        } else {
            state.playedApproachIdle = false;
        }
    }

    private static void tryPlayApproachIdle(OceanSpiderEntity crab, State state) {
        if (state.playedApproachIdle || crab.level().isClientSide) {
            return;
        }
        Player player = crab.level().getNearestPlayer(crab, APPROACH_IDLE_RANGE);
        if (player == null || crab.distanceToSqr(player) > APPROACH_IDLE_RANGE * APPROACH_IDLE_RANGE) {
            return;
        }
        crab.playSound(SoundEvents.SPIDER_AMBIENT, APPROACH_IDLE_VOLUME, crab.getVoicePitch());
        state.playedApproachIdle = true;
    }

    private static Vec3 aimPoint(LivingEntity target) {
        return target.position().add(0.0D, target.getBbHeight() * 0.4D, 0.0D);
    }

    private static State state(OceanSpiderEntity crab) {
        return STATE.computeIfAbsent(crab, ignored -> new State());
    }

    private static final class State {
        boolean airborne;
        boolean hitThisLeap;
        boolean playedApproachIdle;
        int airTicks;
    }
}
