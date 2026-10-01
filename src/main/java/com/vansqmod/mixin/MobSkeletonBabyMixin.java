package com.vansqmod.mixin;

import com.vansqmod.entity.SkeletonBabies;
import com.vansqmod.entity.SkeletonBabyData;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@link Mob#setBaby} is empty and {@link AbstractSkeleton} does not declare an
 * override, so natural / chaos rolls must write the synched baby flag here.
 */
@Mixin(Mob.class)
public abstract class MobSkeletonBabyMixin {

    @Inject(method = "setBaby", at = @At("HEAD"))
    private void vansqmod$skeletonSetBaby(boolean baby, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (!(self instanceof AbstractSkeleton)) {
            return;
        }
        self.getEntityData().set(SkeletonBabyData.BABY, baby);
        if (SkeletonBabies.canTouchChunks(self)) {
            self.refreshDimensions();
            SkeletonBabies.prepareBaby(self);
        }
    }
}
