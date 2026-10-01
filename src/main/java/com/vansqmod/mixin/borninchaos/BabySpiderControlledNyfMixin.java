package com.vansqmod.mixin.borninchaos;

import com.nyfaria.awcapi.ClimberHelper;
import com.nyfaria.awcapi.entity.ClimberComponent;
import com.nyfaria.awcapi.entity.IAdvancedClimber;
import com.nyfaria.awcapi.entity.movement.ClimberPathNavigator;
import com.nyfaria.nyfsspiders.Config;
import com.vansqmod.compat.NyfSpiderAi;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

/**
 * Born in Chaos' tamed Baby Spider extends {@link TamableAnimal}, so Nyf's {@code SpiderMixin}
 * never applies. Mirror that climber + BetterLeap AI here.
 */
@Mixin(targets = "net.mcreator.borninchaosv.entity.BabySpiderControlledEntity", remap = false)
public abstract class BabySpiderControlledNyfMixin extends TamableAnimal implements IAdvancedClimber {

    @Unique
    private ClimberComponent vansqmod$climberComponent;

    @Unique
    private boolean vansqmod$pathFinderDebugPreview;

    @Unique
    private double vansqmod$lerpYRot;

    @Unique
    private double vansqmod$lerpXRot;

    @Unique
    private double vansqmod$lerpYHeadRot;

    @Unique
    private int vansqmod$lerpHeadSteps;

    protected BabySpiderControlledNyfMixin(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void vansqmod$initClimber(CallbackInfo ci) {
        this.vansqmod$climberComponent = new ClimberComponent(this);
        ClimberHelper.initClimber(this);
        NyfSpiderAi.applyFollowRangeBonus(this);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void vansqmod$debugPreview(CallbackInfo ci) {
        this.vansqmod$pathFinderDebugPreview = Config.PATH_FINDER_DEBUG_PREVIEW.get();
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void vansqmod$betterLeap(CallbackInfo ci) {
        NyfSpiderAi.addBetterLeap(this);
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void vansqmod$livingTickClimber(CallbackInfo ci) {
        ClimberHelper.livingTickClimber(this);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void vansqmod$writeClimber(CompoundTag tag, CallbackInfo ci) {
        if (this.vansqmod$climberComponent != null) {
            this.vansqmod$climberComponent.writeToNbt(tag);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void vansqmod$readClimber(CompoundTag tag, CallbackInfo ci) {
        if (this.vansqmod$climberComponent != null) {
            this.vansqmod$climberComponent.readFromNbt(tag);
        }
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        ClimberPathNavigator<BabySpiderControlledNyfMixin> navigator =
                new ClimberPathNavigator<>(this, level, false);
        navigator.setCanFloat(true);
        return navigator;
    }

    @Override
    public void tick() {
        ClimberHelper.tickClimber(this);
        super.tick();
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        ClimberHelper.handleMove(this, type, movement, true);
        super.move(type, movement);
        ClimberHelper.handleMove(this, type, movement, false);
    }

    @Override
    public BlockPos getOnPos() {
        return ClimberHelper.getAdjustedOnPosition(this, super.getOnPos());
    }

    @Override
    public void travel(Vec3 input) {
        if (!ClimberHelper.handleTravel(this, input)) {
            super.travel(input);
        }
        ClimberHelper.postTravel(this, input);
    }

    @Override
    public void jumpFromGround() {
        if (!ClimberHelper.handleJump(this)) {
            super.jumpFromGround();
        }
    }

    @Override
    public void lookAt(EntityAnchorArgument.Anchor anchor, Vec3 target) {
        Vec3 local = this.getOrientation().getLocal(target.subtract(this.position()));
        super.lookAt(anchor, this.position().add(local));
    }

    @Override
    public ClimberComponent getClimberComponent() {
        return this.vansqmod$climberComponent;
    }

    @Override
    public Mob asMob() {
        return this;
    }

    @Override
    public float getMovementSpeed() {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    @Override
    public float getBlockSlipperiness(BlockPos pos) {
        return NyfSpiderAi.blockSlipperiness(this, pos);
    }

    @Override
    public boolean canClimbOnBlock(BlockState state, BlockPos pos) {
        return NyfSpiderAi.canClimbOnBlock(state);
    }

    @Override
    public boolean shouldTrackPathingTargets() {
        return this.vansqmod$pathFinderDebugPreview;
    }

    @Override
    public void setLerpYRot(Float value) {
        this.vansqmod$lerpYRot = value != null ? value : 0.0F;
    }

    @Override
    public void setLerpXRot(Float value) {
        this.vansqmod$lerpXRot = value != null ? value : 0.0F;
    }

    @Override
    public void setLerpYHeadRot(Float value) {
        this.vansqmod$lerpYHeadRot = value != null ? value : 0.0F;
    }

    @Override
    public void setLerpHeadSteps(int steps) {
        this.vansqmod$lerpHeadSteps = steps;
    }

    @Override
    public Direction getGroundSide() {
        return this.getClimberComponent().getGroundSide();
    }

    @Override
    public void onPathingObstructed(Direction direction) {
    }

    @Override
    public int getMaxStuckCheckTicks() {
        return 40;
    }

    @Override
    public float getBridgePathingMalus(Mob mob, BlockPos pos, Node node) {
        return -1.0F;
    }

    @Override
    public float getPathingMalus(
            BlockGetter level,
            Mob mob,
            PathType pathType,
            BlockPos pos,
            Vec3i offset,
            Predicate<Direction> predicate
    ) {
        return NyfSpiderAi.pathingMalus(this, level, mob, pathType, pos, offset, predicate);
    }

    @Override
    public void pathFinderCleanup() {
    }
}
