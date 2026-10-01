package com.vansqmod.mixin.sleeptight;

import com.vansqmod.compat.SleepTightHomeBedSync;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * After a night is counted, record that bed's level and point HOME at the highest one.
 */
@Mixin(targets = "net.mehvahdjukaar.sleep_tight.core.ModEvents", remap = false)
public abstract class SleepTightHomeBedMixin {

    @Inject(method = "onPlayerSleepFinished", at = @At("RETURN"))
    private static void vansqmod$syncHomeBed(ServerPlayer player, long sleptTime, long wakeTime, CallbackInfo ci) {
        SleepTightHomeBedSync.sendSleptBed(player);
    }
}
