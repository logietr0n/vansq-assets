package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import com.vansqmod.network.AstronomySpacePayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = VansqMod.MODID)
public final class AstronomySyncEvents {

    private AstronomySyncEvents() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        if (!server.isDedicatedServer()) {
            return;
        }
        AstronomyWorldData data = AstronomyWorldData.get(server);
        data.ensureDefault(AstronomyWorldData.biomeSeed(server.getLevel(Level.OVERWORLD)));
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        AstronomyWorldData data = AstronomyWorldData.get(server);
        if (!data.hasSnapshot()) {
            if (server.isDedicatedServer()) {
                data.ensureDefault(AstronomyWorldData.biomeSeed(server.getLevel(Level.OVERWORLD)));
            } else {
                return;
            }
        }
        PacketDistributor.sendToPlayer(player, new AstronomySpacePayload(data.snapshot()));
    }

    public static void handleClientSnapshot(ServerPlayer player, String snapshot) {
        if (player == null || snapshot == null || snapshot.isBlank()) {
            return;
        }
        if (snapshot.length() > AstronomySpacePayload.MAX_LENGTH
                || !snapshot.contains("Spyglass Astronomy - Format:")) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        AstronomyWorldData data = AstronomyWorldData.get(server);
        if (snapshot.equals(data.snapshot())) {
            return;
        }
        data.setSnapshot(snapshot);
        PacketDistributor.sendToAllPlayers(new AstronomySpacePayload(snapshot));
    }
}
