package com.vansqmod.mixin;

import com.vansqmod.entity.MobFishingRodAi;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mob AI Tweaks reels every mob hook at ~58 ticks. Misses come back at 10,
 * hits yank the hooked entity at 30. Inject at RETURN so MAIT's cancelled
 * tick still finishes flight/stick physics first.
 */
@Mixin(FishingHook.class)
public abstract class FishingHookMobReelMixin {

    @Inject(method = "tick", at = @At("RETURN"))
    private void vansqmod$reelMobCombatHook(CallbackInfo ci) {
        MobFishingRodAi.tickReel((FishingHook) (Object) this);
    }
}
