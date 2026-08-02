package com.vansqmod.client;

import com.vansqmod.VansqMod;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Hard-blocks Xaero World Map / Minimap screens so they cannot open.
 * HUD rendering and keybinds are cancelled via mixins.
 */
@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class XaeroMapDisableClient {

    private XaeroMapDisableClient() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!ModList.get().isLoaded("xaerominimap") && !ModList.get().isLoaded("xaeroworldmap")) {
            return;
        }
        Screen screen = event.getNewScreen();
        if (screen == null) {
            return;
        }
        String name = screen.getClass().getName();
        if (name.startsWith("xaero.map.gui.") || name.startsWith("xaero.common.gui.")) {
            event.setCanceled(true);
        }
    }
}
