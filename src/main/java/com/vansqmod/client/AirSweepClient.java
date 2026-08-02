package com.vansqmod.client;

import com.vansqmod.VansqMod;
import com.vansqmod.compat.SweepingTagHandler;
import com.vansqmod.network.AirSweepPayload;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Air-sweep without Combat Nouveau: full-strength left-click in empty air sends
 * {@link AirSweepPayload}. Skipped when CN is loaded (it owns that path).
 */
@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class AirSweepClient {

    private AirSweepClient() {
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (ModList.get().isLoaded("combatnouveau")) {
            return;
        }

        if (!(event.getEntity() instanceof LocalPlayer player) || player.isSpectator()) {
            return;
        }
        if (!SweepingTagHandler.isSweepingWeapon(player.getMainHandItem())) {
            return;
        }
        if (player.getAttackStrengthScale(0.5F) < 1.0F || !SweepingTagHandler.meetsSweepStance(player)) {
            return;
        }

        PacketDistributor.sendToServer(new AirSweepPayload());
        player.resetAttackStrengthTicker();
    }
}
