package com.vansqmod.network;

import com.vansqmod.VansqMod;
import com.vansqmod.compat.SweepingTagHandler;
import com.vansqmod.integration.backpacks.BackpackPickupHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = VansqMod.MODID)
public final class ModNetwork {

    private ModNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                BackpackPlacePickupPayload.TYPE,
                BackpackPlacePickupPayload.STREAM_CODEC,
                (payload, ctx) -> {
                    if (!(ctx.player() instanceof ServerPlayer player)) {
                        return;
                    }
                    ctx.enqueueWork(() -> BackpackPickupHandler.tryPlaceOrPickup(player));
                }
        );
        registrar.playToServer(
                AirSweepPayload.TYPE,
                AirSweepPayload.STREAM_CODEC,
                (payload, ctx) -> {
                    if (!(ctx.player() instanceof ServerPlayer player)) {
                        return;
                    }
                    ctx.enqueueWork(() -> SweepingTagHandler.performAirSweep(player));
                }
        );
        registrar.playToClient(
                SandstormVanillaFogPayload.TYPE,
                SandstormVanillaFogPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> handleSandstormFogOnClient(payload))
        );
    }

    private static void handleSandstormFogOnClient(SandstormVanillaFogPayload payload) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        try {
            Class.forName("com.vansqmod.client.SandstormVanillaFogClient")
                    .getMethod("handle", SandstormVanillaFogPayload.class)
                    .invoke(null, payload);
        } catch (ReflectiveOperationException ignored) {
            // Client class / DH unavailable.
        }
    }
}
