package com.vansqmod.mixin.variantsandventures;

import com.faboslav.variantsandventures.common.events.entity.ProjectileHitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla snowball Gelid bonuses are applied after {@code Snowball#onHitEntity}
 * so Amendments freeze cannot cap/overwrite the 120-tick Gelid freeze.
 */
@Mixin(targets = "com.faboslav.variantsandventures.common.entity.event.GelidOnSnowballHitEvent", remap = false)
public abstract class GelidOnSnowballHitMixin {

    @Inject(method = "handleSnowballHit", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$skipVanillaGelidBonus(ProjectileHitEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}
