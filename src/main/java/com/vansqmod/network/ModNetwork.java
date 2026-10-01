package com.vansqmod.network;

import com.vansqmod.VansqMod;
import com.vansqmod.boss.BossBarColorTracker;
import com.vansqmod.compat.AstronomySyncEvents;
import com.vansqmod.compat.SweepingTagHandler;
import com.vansqmod.integration.backpacks.BackpackPickupHandler;
import com.vansqmod.integration.curios.CurioSpyglass;
import com.vansqmod.registry.ModAttachments;
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
                HomeBedPayload.TYPE,
                HomeBedPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> handleHomeBed(payload))
        );
        registrar.playToClient(
                SandstormVanillaFogPayload.TYPE,
                SandstormVanillaFogPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> handleSandstormFogOnClient(payload))
        );
        registrar.playToClient(
                BossBarColorPayload.TYPE,
                BossBarColorPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> handleBossBarColor(payload))
        );
        registrar.playToClient(
                AstronomySpacePayload.TYPE,
                AstronomySpacePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> handleAstronomySpace(payload))
        );
        registrar.playToServer(
                AstronomySpaceUpdatePayload.TYPE,
                AstronomySpaceUpdatePayload.STREAM_CODEC,
                (payload, ctx) -> {
                    if (!(ctx.player() instanceof ServerPlayer player)) {
                        return;
                    }
                    ctx.enqueueWork(() -> AstronomySyncEvents.handleClientSnapshot(player, payload.snapshot()));
                }
        );
        registrar.playToServer(
                CurioSpyglassScopingPayload.TYPE,
                CurioSpyglassScopingPayload.STREAM_CODEC,
                (payload, ctx) -> {
                    if (!(ctx.player() instanceof ServerPlayer player)) {
                        return;
                    }
                    ctx.enqueueWork(() -> player.setData(
                            ModAttachments.CURIO_SPYGLASS_SCOPING.get(),
                            payload.scoping() && CurioSpyglass.has(player)
                    ));
                }
        );
    }

    private static void handleAstronomySpace(AstronomySpacePayload payload) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        try {
            Class.forName("com.vansqmod.client.AstronomyClientSync")
                    .getMethod("handleSnapshot", String.class)
                    .invoke(null, payload.snapshot());
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static void handleBossBarColor(BossBarColorPayload payload) {
        if (payload.present()) {
            BossBarColorTracker.set(payload.barId(), payload.rgb());
        } else {
            BossBarColorTracker.clear(payload.barId());
        }
    }

    private static void handleHomeBed(HomeBedPayload payload) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return;
        }
        try {
            Class.forName("com.vansqmod.client.SleepTightHomeBed")
                    .getMethod("accept", boolean.class, String.class, int.class, int.class, int.class)
                    .invoke(null, payload.present(), payload.dimension(), payload.x(), payload.y(), payload.z());
        } catch (ReflectiveOperationException ignored) {
        }
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
