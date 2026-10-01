package com.vansqmod.mixin.sleeptight;

import com.vansqmod.compat.SleepCompat;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Seamless Sleep sets time back from morning to night, then interpolates
 * forward. Sleep Tight treats every interpolated tick as a /time rewind and
 * spams {@code message.sleep_tight.time_skipped}.
 */
@Mixin(targets = "net.mehvahdjukaar.sleep_tight.core.InsomniaCooldown", remap = false)
public abstract class SleepTightRewindMixin {

    @Inject(method = "tickRewind", at = @At("HEAD"), cancellable = true)
    private void vansqmod$ignoreSeamlessInterpolation(
            ServerPlayer player,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (SleepCompat.isSleepAnimationActive()) {
            cir.setReturnValue(false);
        }
    }
}
