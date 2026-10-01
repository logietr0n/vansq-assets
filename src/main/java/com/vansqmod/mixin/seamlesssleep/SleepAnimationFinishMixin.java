package com.vansqmod.mixin.seamlesssleep;

import com.vansqmod.compat.SleepCompat;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.aqualoco.sec.sleep.SleepAnimationState", remap = false)
public abstract class SleepAnimationFinishMixin {

    @Unique
    private boolean vansqmod$wasActive;

    @Inject(method = "tick", at = @At("HEAD"))
    private void vansqmod$captureActive(ServerLevel level, CallbackInfo ci) {
        vansqmod$wasActive = SleepCompat.isSleepAnimationActive();
        if (vansqmod$wasActive) {
            SleepCompat.markSeamlessWake(level);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void vansqmod$markFinishedWake(ServerLevel level, CallbackInfo ci) {
        if (vansqmod$wasActive && !SleepCompat.isSleepAnimationActive()) {
            SleepCompat.markSeamlessWake(level);
        }
    }
}
