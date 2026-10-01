package com.vansqmod.entity;

import com.vansqmod.registry.ModSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Peaceful skeleton that wanders Glimmering Weald. Pose and walk come from
 * {@code assets/vansqmod/animations/entity/mellowed.animation.json} (BlockBench / GeckoLib).
 */
public class Mellowed extends PathfinderMob implements GeoEntity, SkeletonBabyAccess {

    private static final EntityDataAccessor<Boolean> BABY =
            SynchedEntityData.defineId(Mellowed.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> TEXTURE =
            SynchedEntityData.defineId(Mellowed.class, EntityDataSerializers.STRING);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");

    /** Requested walk speed. */
    public static final double MOVEMENT_SPEED = 0.1D;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean seekingLichenMoss;

    public Mellowed(EntityType<? extends Mellowed> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public boolean isSeekingLichenMoss() {
        return this.seekingLichenMoss;
    }

    public void setSeekingLichenMoss(boolean seekingLichenMoss) {
        this.seekingLichenMoss = seekingLichenMoss;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BABY, false);
        builder.define(TEXTURE, "");
    }

    @Override
    public boolean isBaby() {
        return this.entityData.get(BABY);
    }

    @Override
    public void setBaby(boolean baby) {
        this.entityData.set(BABY, baby);
        if (SkeletonBabies.canTouchChunks(this)) {
            this.refreshDimensions();
            SkeletonBabies.prepareBaby(this);
        }
    }

    @Override
    public float getAgeScale() {
        return this.isBaby() ? 1.0F : super.getAgeScale();
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        if (this.isBaby()) {
            return EntityDimensions.scalable(SkeletonBabies.WIDTH, SkeletonBabies.HEIGHT)
                    .withEyeHeight(SkeletonBabies.EYE_HEIGHT);
        }
        return super.getDefaultDimensions(pose);
    }

    @Override
    public String vansqmod$getSkeletonBabyTexture() {
        return this.entityData.get(TEXTURE);
    }

    @Override
    public void vansqmod$setSkeletonBabyTexture(String texture) {
        this.entityData.set(TEXTURE, texture == null ? "" : texture);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("IsBaby", this.isBaby());
        tag.putString("SkeletonBabyTexture", this.vansqmod$getSkeletonBabyTexture());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("IsBaby")) {
            this.entityData.set(BABY, tag.getBoolean("IsBaby"));
        }
        if (tag.contains("SkeletonBabyTexture")) {
            this.entityData.set(TEXTURE, tag.getString("SkeletonBabyTexture"));
        }
        if (SkeletonBabies.canTouchChunks(this)) {
            this.refreshDimensions();
            SkeletonBabyAttributes.apply(this);
        }
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new MellowedNavigation(this, level);
    }

    /**
     * Stable per-mob head Z tilt in {@code [-5, 5]} degrees, derived from UUID
     * so client and server match without extra sync.
     */
    public float headRollRadians() {
        RandomSource roll = RandomSource.create(
                this.getUUID().getMostSignificantBits() ^ this.getUUID().getLeastSignificantBits() ^ 0x4D45574CL);
        return Mth.DEG_TO_RAD * (roll.nextFloat() * 10.0F - 5.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    public static boolean checkSpawnRules(
            EntityType<Mellowed> type,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random
    ) {
        if (spawnType != MobSpawnType.NATURAL && spawnType != MobSpawnType.CHUNK_GENERATION) {
            return true;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        return MellowedLichenMoss.isStandableNode(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MellowedSeekLichenMossGoal(this));
        this.goalSelector.addGoal(2, new MellowedStrollGoal(this, 1.0D));
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnType,
            SpawnGroupData spawnGroupData
    ) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        this.setCanPickUpLoot(false);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            super.setItemSlot(slot, ItemStack.EMPTY);
        }
        if (!this.level().isClientSide()) {
            SkeletonBabies.tryReplaceWithBabyPair(this, spawnType);
        }
        return data;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
    }

    @Override
    protected void populateDefaultEquipmentEnchantments(
            ServerLevelAccessor level,
            RandomSource random,
            DifficultyInstance difficulty
    ) {
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public void setCanPickUpLoot(boolean canPickup) {
        super.setCanPickUpLoot(false);
    }

    @Override
    public boolean canHoldItem(ItemStack stack) {
        return false;
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return false;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, ItemStack.EMPTY);
    }

    @Override
    public boolean isInvertedHealAndHarm() {
        return true;
    }

    /**
     * Vanilla sets {@code walkAnimation} speed to 1.5 on hit so bipeds flinch.
     * That would play our walk clip; keep the pre-hit speed instead.
     */
    @Override
    public void handleDamageEvent(DamageSource source) {
        float walkSpeed = this.walkAnimation.speed();
        super.handleDamageEvent(source);
        this.walkAnimation.setSpeed(walkSpeed);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundEvents.MELLOWED_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return super.getAmbientSoundInterval() * 4;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSoundEvents.MELLOWED_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundEvents.MELLOWED_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSoundEvents.MELLOWED_STEP.get(), this.getSoundVolume(), this.getVoicePitch());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "move", 5, this::handleAnimations));
    }

    private PlayState handleAnimations(AnimationState<Mellowed> state) {
        // Hurt must not start a walk cycle. Vanilla sets walkAnimation speed to 1.5 on
        // hit, which would otherwise look like limb sway after the flash.
        if (this.hurtTime > 0) {
            state.setControllerSpeed(0.0F);
            return PlayState.CONTINUE;
        }
        state.setControllerSpeed(1.0F);
        if (this.walkAnimation.isMoving()) {
            return state.setAndContinue(WALK);
        }
        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
