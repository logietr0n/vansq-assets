package com.vansqmod.mixin.sleepquality;

import com.vansqmod.compat.SleepCompat;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.fudge.sleepquality.event.ModEvents", remap = false)
public abstract class SleepQualityActualSleepMixin {

    @Inject(method = "wasActualSleep", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$seamlessFinished(
            Player player,
            PlayerWakeUpEvent event,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (SleepCompat.shouldTreatAsActualSleep(player, event)) {
            cir.setReturnValue(true);
        }
    }
}
