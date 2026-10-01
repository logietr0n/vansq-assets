package com.vansqmod.mixin.piglinproliferation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tallestred.piglinproliferation.PPEvents;

@Mixin(PPEvents.class)
public abstract class BucklerDashMomentumMixin {

    @Redirect(
            method = "onLivingTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V",
                    ordinal = 0
            )
    )
    private static void vansqmod$keepDashMomentum(LivingEntity entity, Vec3 movement) {
    }

    @Redirect(
            method = "onJump",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(DDD)V"
            )
    )
    private static void vansqmod$keepJump(LivingEntity entity, double x, double y, double z) {
    }
}
