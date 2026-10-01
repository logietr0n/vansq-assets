package com.vansqmod.mixin.sleeptight;

import com.vansqmod.compat.SleepCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Sleep Tight rolls wake-up encounters during {@code SleepFinishedTimeEvent},
 * before Seamless Sleep's animation starts. A spawned mob waking the player
 * cancels that animation and leaves the night half-skipped.
 */
@Mixin(targets = "net.mehvahdjukaar.sleep_tight.core.WakeUpEncounterHelper", remap = false)
public abstract class SleepTightEncounterDeferMixin {

    @Inject(method = "tryPerformEncounter", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$deferUntilWake(
            ServerPlayer player,
            ServerLevel level,
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!SleepCompat.shouldDeferEncounters()) {
            return;
        }
        SleepCompat.deferEncounter(player, pos);
        cir.setReturnValue(false);
    }
}
