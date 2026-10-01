package com.vansqmod.mixin.sleeptight;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Sleep Tight draws the current clock at 2,2 while sleeping, on top of
 * Seamless Sleep's "Sleeping through the night" overlay.
 */
@Mixin(targets = "net.mehvahdjukaar.sleep_tight.client.SleepGuiOverlay", remap = false)
public abstract class SleepTightTimeHudMixin {

    @Inject(method = "getCurrentTime", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$hideTimeOfDay(Level level, CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(Component.empty());
    }
}
