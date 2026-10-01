package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

@EventBusSubscriber(modid = VansqMod.MODID)
public final class SleepCompatEvents {

    private SleepCompatEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void keepAsleepDuringAnimation(CanContinueSleepingEvent event) {
        if (SleepCompat.isSleepAnimationActive()) {
            event.setContinueSleeping(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void replaySleepTightEncounter(PlayerWakeUpEvent event) {
        if (event.getEntity().level().isClientSide() || event.wakeImmediately()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.getSleepTimer() < 99 && !SleepCompat.shouldTreatAsActualSleep(player, event)) {
            return;
        }
        SleepCompat.replayDeferredEncounter(player);
        SleepCompat.clearSeamlessWake(player);
    }
}
