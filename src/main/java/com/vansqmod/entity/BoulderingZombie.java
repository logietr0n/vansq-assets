package com.vansqmod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import com.vansqmod.registry.ModSoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Cave zombie that replaces leftover vanilla {@code Zombie} spawns (and zombie spawners)
 * below y=0 after Variants and Ventures / vansq biome replacements.
 *
 * <p>Climbing is spider-style once a 2+ block wall is started: stay on the wall
 * until it ends, water, or {@link #MAX_CLIMB_BLOCKS} of rise. 1-block steps and
 * 1-block corners never start a climb so vanilla jumping/pathing handles them.
 * A climb that does not gain height for {@link #STALL_ABORT_TICKS} is aborted so
 * they fall out of crevices instead of hanging there.</p>
 */
public class BoulderingZombie extends Zombie implements GeoEntity {

    /** Geo is a 32-pixel (2 block) zombie; scale 1 so the model is exactly 2 blocks tall. */
    public static final double MODEL_SCALE = 1.0D;
    public static final double MAX_CLIMB_BLOCKS = 8.0D;

    private static final int COLLISION_HOLD_TICKS = 3;
    private static final int STALL_ABORT_TICKS = 40;
    private static final int STALL_COOLDOWN_TICKS = 40;
    private static final double CLIMB_PROGRESS_EPSILON = 0.05D;

    private static final EntityDataAccessor<Boolean> DATA_CLIMBING =
            SynchedEntityData.defineId(BoulderingZombie.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private double climbAnchorY;
    private double lastClimbProgressY;
    private boolean climbCommitted;
    private int climbHoldTicks;
    private int climbStallTicks;
    private int climbCooldownTicks;
    private Direction climbFace = Direction.NORTH;

    public BoulderingZombie(EntityType<? extends BoulderingZombie> type, Level level) {
        super(type, level);
        applyModelScale();
    }

    private void applyModelScale() {
        AttributeInstance scale = this.getAttribute(Attributes.SCALE);
        if (scale != null) {
            scale.setBaseValue(MODEL_SCALE);
        }
        AttributeInstance knockback = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (knockback != null) {
            knockback.setBaseValue(0.15D);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 26.0D)
                .add(Attributes.ARMOR, 3.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.15D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new BoulderingZombieClimbGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CLIMBING, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            updateClimbingState();
        }
    }

    /**
     * Same rule as {@link net.minecraft.world.entity.monster.Spider} once a climb has
     * started: stay on the wall while pressed into it. Unlike spiders, 1-block steps
     * never start a climb, and climbing stops after 8 blocks of rise or 2 seconds
     * without gaining height.
     */
    private void updateClimbingState() {
        if (this.climbCooldownTicks > 0) {
            this.climbCooldownTicks--;
            if (this.climbCommitted || this.isClimbing()) {
                stopClimbing();
            }
            return;
        }

        if (this.isInWater()) {
            stopClimbing();
            this.climbAnchorY = this.getY();
            return;
        }

        if (this.onGround() && !isTallWallAhead()) {
            // On ground against a 1-block step/corner: never keep a climb. Vanilla
            // jumping has to be able to take over or they wedge in the corner.
            if (this.climbCommitted || this.isClimbing()) {
                stopClimbing();
            }
            this.climbAnchorY = this.getY();
            return;
        }

        boolean againstWall = this.horizontalCollision || isTouchingClimbableFace();
        if (againstWall) {
            this.climbFace = findWallFace();
            this.climbHoldTicks = COLLISION_HOLD_TICKS;
        } else if (this.climbHoldTicks > 0) {
            this.climbHoldTicks--;
        }
        boolean holdClimb = againstWall || this.climbHoldTicks > 0;

        if (!this.climbCommitted) {
            if (this.onGround()) {
                this.climbAnchorY = this.getY();
            }
            if (this.horizontalCollision && isTallWallAhead()) {
                beginClimb();
            }
        } else if (this.getY() - this.climbAnchorY >= MAX_CLIMB_BLOCKS) {
            stopClimbing();
            return;
        } else if (!holdClimb) {
            stopClimbing();
            return;
        } else if (!hasClimbHeightProgress()) {
            abortStalledClimb();
            return;
        }

        this.setClimbing(this.climbCommitted);
        if (this.climbCommitted) {
            // LivingEntity only applies the 0.2 climb boost when colliding OR jumping.
            // JumpControl wipes LivingEntity.jumping each tick, so request a jump here
            // for the next travel pass in case collision flickers.
            this.getJumpControl().jump();
        }
    }

    private void beginClimb() {
        this.climbCommitted = true;
        this.climbHoldTicks = COLLISION_HOLD_TICKS;
        this.lastClimbProgressY = this.getY();
        this.climbStallTicks = 0;
    }

    private boolean hasClimbHeightProgress() {
        if (this.getY() > this.lastClimbProgressY + CLIMB_PROGRESS_EPSILON) {
            this.lastClimbProgressY = this.getY();
            this.climbStallTicks = 0;
            return true;
        }
        this.climbStallTicks++;
        return this.climbStallTicks < STALL_ABORT_TICKS;
    }

    private void abortStalledClimb() {
        stopClimbing();
        this.climbCooldownTicks = STALL_COOLDOWN_TICKS;
    }

    private void stopClimbing() {
        this.climbCommitted = false;
        this.climbHoldTicks = 0;
        this.climbStallTicks = 0;
        this.setClimbing(false);
    }

    boolean isClimbCoolingDown() {
        return this.climbCooldownTicks > 0;
    }

    Direction getClimbFace() {
        return this.climbFace;
    }

    /**
     * Still next to the wall we are climbing. A 1-block step (solid at feet, air
     * above) does not count; those must jump, not climb.
     */
    boolean isTouchingClimbableFace() {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos front = this.blockPosition().relative(dir);
            if (isSolidObstacle(front) && (this.climbCommitted || isSolidObstacle(front.above()))) {
                return true;
            }
        }
        return false;
    }

    private Direction findWallFace() {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (isTallWall(dir)) {
                return dir;
            }
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (isSolidObstacle(this.blockPosition().relative(dir))) {
                return dir;
            }
        }
        LivingEntity target = this.getTarget();
        if (target != null) {
            Direction toward = Direction.getNearest(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
            if (toward.getAxis() != Direction.Axis.Y) {
                return toward;
            }
        }
        return this.getDirection();
    }

    boolean isTallWallAhead() {
        Direction facing = this.getDirection();
        if (isTallWall(facing)) {
            return true;
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (isTallWall(dir)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A 1-block step is solid at foot height with open space above it. A climbable
     * wall is solid at foot height <em>and</em> the block above that.
     */
    boolean isTallWall(Direction dir) {
        BlockPos front = this.blockPosition().relative(dir);
        return isSolidObstacle(front) && isSolidObstacle(front.above());
    }

    boolean hasTallWallToward(LivingEntity target) {
        Direction toward = Direction.getNearest(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
        if (toward.getAxis() == Direction.Axis.Y) {
            toward = this.getDirection();
        }
        return isTallWall(toward);
    }

    private boolean isSolidObstacle(BlockPos pos) {
        return !this.level().getBlockState(pos).getCollisionShape(this.level(), pos).isEmpty();
    }

    public boolean isClimbing() {
        return this.entityData.get(DATA_CLIMBING);
    }

    public void setClimbing(boolean climbing) {
        this.entityData.set(DATA_CLIMBING, climbing);
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbing() || super.onClimbable();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundEvents.BOULDERING_ZOMBIE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSoundEvents.BOULDERING_ZOMBIE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundEvents.BOULDERING_ZOMBIE_DEATH.get();
    }

    @Override
    protected SoundEvent getStepSound() {
        return ModSoundEvents.BOULDERING_ZOMBIE_STEP.get();
    }

    @Override
    public float getVoicePitch() {
        return 1.0F;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Procedural posing in BoulderingZombieModel, same as Putrid.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
