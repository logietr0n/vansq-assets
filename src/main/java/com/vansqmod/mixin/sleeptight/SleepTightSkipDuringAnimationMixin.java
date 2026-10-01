package com.vansqmod.mixin.sleeptight;

import com.vansqmod.compat.SleepCompat;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Seamless Sleep keeps players in bed while it interpolates time, so vanilla
 * still thinks a night skip can happen every tick. Sleep Tight then reapplies
 * hunger, insomnia resets, and nightmare rolls for each of those skips.
 */
@Mixin(targets = "net.mehvahdjukaar.sleep_tight.core.ModEvents", remap = false)
public abstract class SleepTightSkipDuringAnimationMixin {

    @Inject(method = "getWakeUpTimeWhenSlept", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$skipWhileAnimating(
            ServerLevel level,
            long newTime,
            CallbackInfoReturnable<Long> cir
    ) {
        if (SleepCompat.isSleepAnimationActive()) {
            cir.setReturnValue(newTime);
        }
    }
}
