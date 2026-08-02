package com.vansqmod.client;

import com.vansqmod.VansqMod;
import com.vansqmod.config.VansqModClientConfigs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/**
 * Reloads client JSON configs when joining a world so edits apply after rejoin.
 */
@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class CustomTooltipReloadHandler {

    private CustomTooltipReloadHandler() {
    }

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        VansqModClientConfigs.loadAll();
    }
}
