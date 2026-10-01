package com.vansqmod.mixin;

import com.vansqmod.entity.SkeletonBabyData;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Skeleton, Stray, Bogged, and Barched Parched override {@code defineSynchedData}
 * and {@code invokespecial} past a method added on AbstractSkeleton. Define the
 * baby accessors from LivingEntity so every subclass actually registers them.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntitySkeletonBabyDataMixin {

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void vansqmod$defineSkeletonBabyData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        if ((Object) this instanceof AbstractSkeleton) {
            builder.define(SkeletonBabyData.BABY, false);
            builder.define(SkeletonBabyData.TEXTURE, "");
        }
    }

    /**
     * Spawn eggs keep the child only when {@code isBaby()} is true after {@code setBaby(true)}.
     * AbstractSkeleton does not declare {@code isBaby()}, so this replaces LivingEntity's constant false.
     */
    @Inject(method = "isBaby", at = @At("RETURN"), cancellable = true)
    private void vansqmod$skeletonIsBaby(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof AbstractSkeleton) {
            cir.setReturnValue(((LivingEntity) (Object) this).getEntityData().get(SkeletonBabyData.BABY));
        }
    }

    @Inject(method = "getVoicePitch", at = @At("RETURN"), cancellable = true)
    private void vansqmod$skeletonBabyPitch(CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof AbstractSkeleton && self.getEntityData().get(SkeletonBabyData.BABY)) {
            cir.setReturnValue((self.getRandom().nextFloat() - self.getRandom().nextFloat()) * 0.2F + 1.5F);
        }
    }
}
