package com.vansqmod.entity;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * When the target is more than one block above, walk into a 2+ block wall so
 * spider-style climbing can take over. 1-block rises and 1-block corners are
 * left to vanilla zombie pathfinding/jumping.
 */
public class BoulderingZombieClimbGoal extends Goal {

    private static final double MIN_CLIMB_HEIGHT = 1.5D;
    private static final double SEEK_RANGE = 16.0D;

    private final BoulderingZombie zombie;

    public BoulderingZombieClimbGoal(BoulderingZombie zombie) {
        this.zombie = zombie;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.zombie.getTarget();
        if (target == null || !target.isAlive() || this.zombie.isClimbCoolingDown()) {
            return false;
        }
        if (this.zombie.isClimbing()) {
            return true;
        }
        double dy = target.getY() - this.zombie.getY();
        if (dy <= MIN_CLIMB_HEIGHT || dy > BoulderingZombie.MAX_CLIMB_BLOCKS) {
            return false;
        }
        return horizontalDistanceSqr(target) <= SEEK_RANGE * SEEK_RANGE;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.zombie.getTarget();
        if (target == null || !target.isAlive() || this.zombie.isClimbCoolingDown()) {
            return false;
        }
        if (this.zombie.isClimbing()) {
            return true;
        }
        double dy = target.getY() - this.zombie.getY();
        if (dy <= 1.0D || dy > BoulderingZombie.MAX_CLIMB_BLOCKS + 1.0D) {
            return false;
        }
        return horizontalDistanceSqr(target) <= (SEEK_RANGE + 2.0D) * (SEEK_RANGE + 2.0D);
    }

    @Override
    public void stop() {
        this.zombie.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = this.zombie.getTarget();
        if (target == null) {
            return;
        }
        this.zombie.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (this.zombie.isClimbing()) {
            this.zombie.getNavigation().stop();
            Direction face = this.zombie.getClimbFace();
            this.zombie.getMoveControl().setWantedPosition(
                    this.zombie.getX() + face.getStepX(),
                    this.zombie.getY() + 1.0D,
                    this.zombie.getZ() + face.getStepZ(),
                    1.0D);
            return;
        }
        if (this.zombie.hasTallWallToward(target) || this.zombie.isTallWallAhead()) {
            this.zombie.getNavigation().stop();
            this.zombie.getMoveControl().setWantedPosition(target.getX(), this.zombie.getY(), target.getZ(), 1.0D);
            return;
        }
        // 1-block step or corner: jump and path, never ram into it as if it were a wall.
        if (this.zombie.horizontalCollision && this.zombie.onGround()) {
            this.zombie.getJumpControl().jump();
        }
        if (!this.zombie.getNavigation().moveTo(target, 1.0D)) {
            this.zombie.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0D);
        }
    }

    private double horizontalDistanceSqr(LivingEntity target) {
        double dx = target.getX() - this.zombie.getX();
        double dz = target.getZ() - this.zombie.getZ();
        return dx * dx + dz * dz;
    }
}
