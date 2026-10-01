package com.vansqmod.mixin.variantsandventures;

import com.vansqmod.compat.VerdantCombat;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Marks the current thread while a Verdant arrow applies damage so knockback can be scaled
 * without also nerfing Verdant melee.
 */
@Mixin(AbstractArrow.class)
public abstract class VerdantArrowHitMixin {

    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void vansqmod$beginVerdantArrowHit(EntityHitResult result, CallbackInfo ci) {
        VerdantCombat.beginArrowHit((AbstractArrow) (Object) this);
    }

    @Inject(method = "onHitEntity", at = @At("RETURN"))
    private void vansqmod$endVerdantArrowHit(EntityHitResult result, CallbackInfo ci) {
        VerdantCombat.endArrowHit((AbstractArrow) (Object) this);
    }
}
