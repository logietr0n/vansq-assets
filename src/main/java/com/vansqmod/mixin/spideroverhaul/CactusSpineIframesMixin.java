package com.vansqmod.mixin.spideroverhaul;

import com.vansqmod.compat.SpiderOverhaulCombat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Spider Overhaul zeros {@code invulnerableTime} for cactus-spine damage, so a
 * death burst can land every projectile on one target in a single tick. Skip
 * already-hurt entities so later spines fly past instead of stacking.
 */
@Mixin(AbstractArrow.class)
public abstract class CactusSpineIframesMixin {

    @Inject(method = "canHitEntity", at = @At("RETURN"), cancellable = true)
    private void vansqmod$cactusSpineRespectIframes(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!SpiderOverhaulCombat.isCactusSpine((Entity) (Object) this)) {
            return;
        }
        AbstractArrow spine = (AbstractArrow) (Object) this;
        if (entity == spine.getOwner()) {
            cir.setReturnValue(false);
            return;
        }
        if (!cir.getReturnValueZ()
                || !(entity instanceof LivingEntity living)
                || living.invulnerableTime <= 10) {
            return;
        }
        cir.setReturnValue(false);
    }
}
