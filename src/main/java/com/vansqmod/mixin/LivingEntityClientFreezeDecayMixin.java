package com.vansqmod.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla only decays {@code ticksFrozen} on the server. The powder-snow GUI overlay
 * reads the client value, so a missed sync (or a client-side add) leaves the overlay
 * up after freeze damage / icy has ended.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityClientFreezeDecayMixin {

    @Inject(
            method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;removeFrost()V")
    )
    private void vansqmod$decayFreezeOnClient(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level().isClientSide || self.isDeadOrDying()) {
            return;
        }
        int frozen = self.getTicksFrozen();
        if (self.isInPowderSnow && self.canFreeze()) {
            self.setTicksFrozen(Math.min(self.getTicksRequiredToFreeze(), frozen + 1));
        } else {
            self.setTicksFrozen(Math.max(0, frozen - 2));
        }
    }
}
