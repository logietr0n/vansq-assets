package com.vansqmod.client;

import com.vansqmod.VansqMod;
import com.vansqmod.boss.BossBarColorTracker;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class BossBarClientEvents {

    private BossBarClientEvents() {
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        BossBarColorTracker.reset();
    }
}
