package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = VansqMod.MODID)
public final class SleepTightHomeBedEvents {

    private SleepTightHomeBedEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ModList.get().isLoaded("sleep_tight") || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        SleepTightHomeBedSync.sendKnownBed(player);
    }
}
