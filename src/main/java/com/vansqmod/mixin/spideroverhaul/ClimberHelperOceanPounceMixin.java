package com.vansqmod.mixin.spideroverhaul;

import com.nyfaria.awcapi.ClimberHelper;
import com.nyfaria.awcapi.entity.IAdvancedClimber;
import com.vansqmod.compat.OceanSpiderLeap;
import dev.chybx.spideroverhaul.entity.OceanSpiderEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Nyf replaces vanilla {@code LeapAtTargetGoal} with BetterLeap, but Spider Crab pounces
 * through {@code CrabPounceGoal} (setDeltaMovement, not leap-at-target). Skip climber
 * travel/move/jump while that pounce is active so wall-climbing does not eat the attack.
 */
@Mixin(value = ClimberHelper.class, remap = false)
public abstract class ClimberHelperOceanPounceMixin {

    @Inject(method = "handleTravel", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$skipTravelDuringPounce(
            IAdvancedClimber climber,
            Vec3 input,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (vansqmod$skipClimber(climber)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "postTravel", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$skipPostTravelDuringPounce(IAdvancedClimber climber, Vec3 input, CallbackInfo ci) {
        if (vansqmod$skipClimber(climber)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleMove", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$skipMoveDuringPounce(
            IAdvancedClimber climber,
            MoverType type,
            Vec3 movement,
            boolean pre,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (vansqmod$skipClimber(climber)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "handleJump", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$skipJumpDuringPounce(
            IAdvancedClimber climber,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (vansqmod$skipClimber(climber)) {
            cir.setReturnValue(false);
        }
    }

    private static boolean vansqmod$skipClimber(IAdvancedClimber climber) {
        Mob mob = climber.asMob();
        return mob instanceof OceanSpiderEntity ocean
                && (ocean.isLeaping() || OceanSpiderLeap.isDescending(ocean));
    }
}
