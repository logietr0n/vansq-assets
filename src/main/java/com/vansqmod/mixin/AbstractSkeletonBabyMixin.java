package com.vansqmod.mixin;

import com.vansqmod.entity.SkeletonBabies;
import com.vansqmod.entity.SkeletonBabyAi;
import com.vansqmod.entity.SkeletonBabyAccess;
import com.vansqmod.entity.SkeletonBabyAttributes;
import com.vansqmod.entity.SkeletonBabyData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonBabyMixin extends Monster implements GeoEntity, SkeletonBabyAccess {

    @Shadow
    protected abstract SoundEvent getStepSound();

    @Shadow
    public abstract void reassessWeaponGoal();

    @Unique
    private static final EntityDataAccessor<Boolean> vansqmod$BABY = SkeletonBabyData.BABY;

    @Unique
    private static final EntityDataAccessor<String> vansqmod$TEXTURE = SkeletonBabyData.TEXTURE;

    @Unique
    private final AnimatableInstanceCache vansqmod$geoCache = GeckoLibUtil.createInstanceCache(this);

    @Unique
    private boolean vansqmod$swinging;

    @Unique
    private long vansqmod$lastSwing;

    protected AbstractSkeletonBabyMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isBaby() {
        return SkeletonBabies.isMarkedBaby(this);
    }

    @Override
    public float getVoicePitch() {
        if (SkeletonBabies.isMarkedBaby(this)) {
            return (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.5F;
        }
        return super.getVoicePitch();
    }

    @Override
    public void setBaby(boolean baby) {
        this.getEntityData().set(vansqmod$BABY, baby);
        if (SkeletonBabies.canTouchChunks(this)) {
            this.refreshDimensions();
            SkeletonBabies.prepareBaby(this);
        }
        if (baby) {
            this.xpReward = ((Object) this) instanceof WitherSkeleton ? 5 : 3;
        }
    }

    @Override
    public boolean canPickUpLoot() {
        return !this.isBaby() && super.canPickUpLoot();
    }

    @Override
    public boolean canHoldItem(ItemStack stack) {
        return !this.isBaby() && super.canHoldItem(stack);
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        return !this.isBaby() && super.wantsToPickUp(stack);
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return !this.isBaby() && super.canUseSlot(slot);
    }

    @Inject(method = "setItemSlot", at = @At("HEAD"), cancellable = true)
    private void vansqmod$blockBabyEquipment(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        if (!SkeletonBabies.isMarkedBaby(this) || stack.isEmpty()) {
            return;
        }
        super.setItemSlot(slot, ItemStack.EMPTY);
        if (!this.level().isClientSide()) {
            this.reassessWeaponGoal();
        }
        ci.cancel();
    }

    @Inject(method = "reassessWeaponGoal", at = @At("RETURN"))
    private void vansqmod$babyAi(CallbackInfo ci) {
        SkeletonBabyAi.apply((AbstractSkeleton) (Object) this);
    }

    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void vansqmod$babyStepPitch(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (SkeletonBabies.isMarkedBaby(this)) {
            this.playSound(this.getStepSound(), 0.15F, this.getVoicePitch());
            ci.cancel();
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
    public boolean hurt(DamageSource source, float amount) {
        if (this.isBaby() && !(((Object) this) instanceof WitherSkeleton) && source.is(DamageTypes.DROWN)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Inject(method = "populateDefaultEquipmentSlots", at = @At("HEAD"), cancellable = true)
    private void vansqmod$skipBabyGear(net.minecraft.util.RandomSource random, DifficultyInstance difficulty, CallbackInfo ci) {
        if (this.isBaby()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                this.setItemSlot(slot, ItemStack.EMPTY);
            }
            ci.cancel();
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void vansqmod$saveBaby(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("IsBaby", this.isBaby());
        tag.putString("SkeletonBabyTexture", this.vansqmod$getSkeletonBabyTexture());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void vansqmod$loadBaby(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("IsBaby")) {
            this.entityData.set(vansqmod$BABY, tag.getBoolean("IsBaby"));
        }
        if (tag.contains("SkeletonBabyTexture")) {
            this.entityData.set(vansqmod$TEXTURE, tag.getString("SkeletonBabyTexture"));
        }
        if (SkeletonBabies.canTouchChunks(this)) {
            this.refreshDimensions();
            SkeletonBabyAttributes.apply(this);
        }
        if (this.isBaby()) {
            this.xpReward = ((Object) this) instanceof WitherSkeleton ? 5 : 3;
        }
    }

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void vansqmod$rollBaby(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnType,
            SpawnGroupData spawnGroupData,
            CallbackInfoReturnable<SpawnGroupData> cir
    ) {
        if (!this.level().isClientSide()) {
            SkeletonBabies.tryReplaceWithBabyPair(this, spawnType);
        }
    }

    @Inject(method = "canFireProjectileWeapon", at = @At("HEAD"), cancellable = true)
    private void vansqmod$babyNoBow(ProjectileWeaponItem weapon, CallbackInfoReturnable<Boolean> cir) {
        if (this.isBaby()) {
            cir.setReturnValue(false);
        }
    }

    @Override
    public String vansqmod$getSkeletonBabyTexture() {
        return this.entityData.get(vansqmod$TEXTURE);
    }

    @Override
    public void vansqmod$setSkeletonBabyTexture(String texture) {
        this.entityData.set(vansqmod$TEXTURE, texture == null ? "" : texture);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<AbstractSkeletonBabyMixin>(this, "move", 0, state -> {
            if (!SkeletonBabies.isMarkedBaby(this)) {
                return PlayState.STOP;
            }
            AbstractSkeleton self = (AbstractSkeleton) (Object) this;
            boolean still = !state.isMoving()
                    && state.getLimbSwingAmount() > -0.15F
                    && state.getLimbSwingAmount() < 0.15F;
            if (!still) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            }
            if (self.isDeadOrDying()) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("death"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop(SkeletonBabies.idleAnimation(self)));
        }));
        controllers.add(new AnimationController<AbstractSkeletonBabyMixin>(this, "attack", 0, state -> {
            if (!SkeletonBabies.isMarkedBaby(this)) {
                return PlayState.STOP;
            }
            if (this.getAttackAnim(state.getPartialTick()) > 0.0F && !this.vansqmod$swinging) {
                this.vansqmod$swinging = true;
                this.vansqmod$lastSwing = this.level().getGameTime();
            }
            if (this.vansqmod$swinging && this.vansqmod$lastSwing + 7L < this.level().getGameTime()) {
                this.vansqmod$swinging = false;
            }
            if (this.vansqmod$swinging
                    && state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                state.getController().forceAnimationReset();
                return state.setAndContinue(RawAnimation.begin().thenPlay("attack"));
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.vansqmod$geoCache;
    }
}
