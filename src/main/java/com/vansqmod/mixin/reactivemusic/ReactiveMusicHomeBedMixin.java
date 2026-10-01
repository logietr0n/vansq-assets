package com.vansqmod.mixin.reactivemusic;

import com.vansqmod.client.SleepTightHomeBed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces Reactive Music's saved sleep position with Sleep Tight's home bed.
 */
@Mixin(targets = "circuitlord.reactivemusic.SongPicker", remap = false)
public abstract class ReactiveMusicHomeBedMixin {

    @Inject(method = "tickEventMap", at = @At("RETURN"))
    private static void vansqmod$homeBed(CallbackInfo ci) {
        SleepTightHomeBed.overwriteHomeEvent();
    }
}
