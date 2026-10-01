package com.vansqmod.mixin.spideroverhaul;

import com.vansqmod.compat.OceanSpiderLeap;
import com.vansqmod.compat.SpiderOverhaulCombat;
import dev.chybx.spideroverhaul.entity.AbstractVariantSpiderEntity;
import dev.chybx.spideroverhaul.entity.OceanSpiderEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.chybx.spideroverhaul.entity.OceanSpiderEntity")
public abstract class OceanSpiderLeapMixin extends AbstractVariantSpiderEntity {

    protected OceanSpiderLeapMixin(EntityType<? extends Spider> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void vansqmod$waterPathfinding(CallbackInfo ci) {
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"), remap = false)
    private void vansqmod$targetGiantSquid(CallbackInfo ci) {
        SpiderOverhaulCombat.addGiantSquidTarget((Mob) (Object) this);
    }

    @Inject(method = "canLeap", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$waterLeapFromSeafloor(CallbackInfoReturnable<Boolean> cir) {
        OceanSpiderEntity crab = (OceanSpiderEntity) (Object) this;
        if (!crab.isInWater()) {
            return;
        }
        cir.setReturnValue(OceanSpiderLeap.canLeapFromSeafloor(crab));
    }

    @Inject(method = "onVariantServerTick", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$tickWaterLeap(CallbackInfo ci) {
        OceanSpiderEntity crab = (OceanSpiderEntity) (Object) this;
        if (!crab.isInWater() && !OceanSpiderLeap.isDescending(crab)) {
            OceanSpiderLeap.tickApproachIdle(crab);
            return;
        }
        OceanSpiderLeap.tick(crab);
        ci.cancel();
    }

    @Override
    protected AABB getAttackBoundingBox() {
        return super.getAttackBoundingBox().inflate(OceanSpiderLeap.ATTACK_REACH_BONUS, 0.0D, OceanSpiderLeap.ATTACK_REACH_BONUS);
    }

    @Override
    public void travel(Vec3 input) {
        if (OceanSpiderLeap.handleTravel((OceanSpiderEntity) (Object) this)) {
            return;
        }
        super.travel(input);
    }

    @Override
    protected float getWaterSlowDown() {
        return this.onGround() ? 1.0F : super.getWaterSlowDown();
    }

    @Override
    public Vec3 getFluidFallingAdjustedMovement(double gravity, boolean falling, Vec3 delta) {
        if (this.onGround()) {
            return super.getFluidFallingAdjustedMovement(gravity, falling, delta);
        }
        return super.getFluidFallingAdjustedMovement(gravity * OceanSpiderLeap.SINK_GRAVITY_SCALE, falling, delta);
    }
}
