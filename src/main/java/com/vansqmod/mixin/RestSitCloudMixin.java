package com.vansqmod.mixin;

import com.vansqmod.entity.MobRestSit;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Flatten Mob AI Tweaks rest clouds to a normal 0.5-block seat. A sliver-thin box
 * sat inside the floor and riders dismounted immediately.
 */
@Mixin(AreaEffectCloud.class)
public abstract class RestSitCloudMixin {

    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void vansqmod$flatRestSeat(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        AreaEffectCloud self = (AreaEffectCloud) (Object) this;
        if (MobRestSit.isRestSeatCloud(self)) {
            cir.setReturnValue(EntityDimensions.scalable(0.5F, 0.5F));
        }
    }
}
