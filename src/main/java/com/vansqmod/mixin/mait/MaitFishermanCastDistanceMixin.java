package com.vansqmod.mixin.mait;

import com.vansqmod.entity.MobFishingRodAi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Mob AI Tweaks fishermen refuse to cast inside 6 blocks. Match Putrid's 1-block floor.
 */
@Mixin(targets = "com.notunanancyowen.mait.mixin.ZombieVillagerEntityMixin$1", remap = false)
public abstract class MaitFishermanCastDistanceMixin {

    @ModifyConstant(method = "tick", constant = @Constant(doubleValue = 36.0D), remap = false, require = 0)
    private double vansqmod$minCastDistanceTick(double original) {
        return MobFishingRodAi.MIN_CAST_DISTANCE_SQR;
    }

    @ModifyConstant(method = "method_6268", constant = @Constant(doubleValue = 36.0D), remap = false, require = 0)
    private double vansqmod$minCastDistanceIntermediary(double original) {
        return MobFishingRodAi.MIN_CAST_DISTANCE_SQR;
    }
}
