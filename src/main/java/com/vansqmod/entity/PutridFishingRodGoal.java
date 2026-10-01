package com.vansqmod.entity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Casts a fishing hook the same way Mob AI Tweaks' zombie villager fisherman does:
 * visible target 1–13 blocks away, once every 60 ticks while on the ground.
 * Flight and sticking still come from MAIT's bobber mixin; reel timing is
 * {@link MobFishingRodAi}.
 */
public class PutridFishingRodGoal extends Goal {

    private final Putrid putrid;

    public PutridFishingRodGoal(Putrid putrid) {
        this.putrid = putrid;
    }

    @Override
    public boolean canUse() {
        return holdsFishingRod() && this.putrid.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.putrid.getTarget();
        if (target == null || !holdsFishingRod()) {
            return;
        }
        double distSqr = this.putrid.distanceToSqr(target);
        if (!this.putrid.hasLineOfSight(target)
                || distSqr <= MobFishingRodAi.MIN_CAST_DISTANCE_SQR
                || distSqr >= MobFishingRodAi.MAX_CAST_DISTANCE_SQR
                || this.putrid.tickCount % MobFishingRodAi.CAST_INTERVAL_TICKS != 0
                || !this.putrid.onGround()) {
            return;
        }
        castAt(target);
    }

    private boolean holdsFishingRod() {
        return MobFishingRodAi.holdsFishingRod(this.putrid);
    }

    private void castAt(LivingEntity target) {
        Level level = this.putrid.level();
        FishingHook hook = new FishingHook(EntityType.FISHING_BOBBER, level);
        hook.setPos(this.putrid.position().add(this.putrid.getLookAngle()));
        Vec3 velocity = target.position()
                .add(target.getDeltaMovement())
                .subtract(this.putrid.position())
                .normalize()
                .scale(MobFishingRodAi.CAST_SPEED);
        hook.setDeltaMovement(velocity);
        hook.setOwner(this.putrid);
        level.addFreshEntity(hook);
        this.putrid.startUsingItem(InteractionHand.MAIN_HAND);
        this.putrid.swing(InteractionHand.MAIN_HAND);
    }
}
