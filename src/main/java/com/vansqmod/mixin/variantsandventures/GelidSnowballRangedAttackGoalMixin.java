package com.vansqmod.mixin.variantsandventures;

import com.faboslav.variantsandventures.common.entity.mob.GelidEntity;
import com.vansqmod.compat.GelidSnowballs;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.faboslav.variantsandventures.common.entity.ai.GelidSnowballRangedAttackGoal")
public abstract class GelidSnowballRangedAttackGoalMixin {

    @Shadow
    @Final
    private GelidEntity gelid;

    @Shadow
    private LivingEntity target;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void vansqmod$allowIceSnowball(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity living = this.gelid.getTarget();
        if (living == null || !living.isAlive() || !GelidSnowballs.isIceSnowball(this.gelid.getOffhandItem())) {
            return;
        }
        this.target = living;
        cir.setReturnValue(true);
    }
}
