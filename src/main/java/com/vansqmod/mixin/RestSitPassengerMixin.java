package com.vansqmod.mixin;

import com.vansqmod.entity.MobRestSit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Rest-sit riders use {@link MobRestSit#attachmentForPassenger} so vanilla
 * zombies (and Putrid) sit at the same ground height. Tiny Takeover's unique
 * baby undead meshes sit higher than 0.5-scaled geo babies (Putrid / Bouldering).
 * The ground-sitting look comes from vanilla's riding leg-fold pose; there is no
 * extra renderer-side drop (see {@link MobRestSit} for why one was removed).
 */
@Mixin(Entity.class)
public abstract class RestSitPassengerMixin {

    @Inject(method = "getPassengerAttachmentPoint", at = @At("HEAD"), cancellable = true)
    private void vansqmod$groundRestSit(
            Entity passenger,
            EntityDimensions dimensions,
            float scale,
            CallbackInfoReturnable<Vec3> cir
    ) {
        if (MobRestSit.isRestSeat((Entity) (Object) this, passenger)) {
            cir.setReturnValue(MobRestSit.attachmentForPassenger((Entity) (Object) this, passenger));
        }
    }
}
