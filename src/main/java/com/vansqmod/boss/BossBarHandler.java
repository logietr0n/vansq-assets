package com.vansqmod.boss;

import com.vansqmod.VansqMod;
import com.vansqmod.network.BossBarColorPayload;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Attaches a vanilla {@link ServerBossEvent} to registered mobs so the HUD bar
 * (and Reactive Music's BOSS event) stay in sync with tracking players.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class BossBarHandler {

    private static final Map<UUID, ServerBossEvent> BARS = new HashMap<>();
    private static final Map<UUID, Set<UUID>> TRACKERS = new HashMap<>();

    private BossBarHandler() {
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(event.getTarget() instanceof LivingEntity living) || !living.isAlive()) {
            return;
        }
        BossBarStyle style = BossBarRegistry.get(living);
        if (style == null) {
            return;
        }
        TRACKERS.computeIfAbsent(living.getUUID(), id -> new HashSet<>()).add(player.getUUID());
        BossFightState.tickServer(living);
        if (!BossFightState.isFightActive(living)) {
            return;
        }
        ServerBossEvent bar = getOrCreate(living, style);
        bar.addPlayer(player);
        PacketDistributor.sendToPlayer(player, BossBarColorPayload.set(bar.getId(), style.colorRgb()));
    }

    @SubscribeEvent
    public static void onStopTracking(PlayerEvent.StopTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        UUID entityId = event.getTarget().getUUID();
        Set<UUID> trackers = TRACKERS.get(entityId);
        if (trackers != null) {
            trackers.remove(player.getUUID());
            if (trackers.isEmpty()) {
                TRACKERS.remove(entityId);
            }
        }
        ServerBossEvent bar = BARS.get(entityId);
        if (bar == null) {
            return;
        }
        PacketDistributor.sendToPlayer(player, BossBarColorPayload.clear(bar.getId()));
        bar.removePlayer(player);
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living) || living.level().isClientSide()) {
            return;
        }
        BossBarStyle style = BossBarRegistry.get(living);
        if (style == null) {
            return;
        }
        BossFightState.tickServer(living);
        if (BossFightState.isFightActive(living)) {
            boolean created = !BARS.containsKey(living.getUUID());
            ServerBossEvent bar = getOrCreate(living, style);
            if (created) {
                addTrackingPlayers(living, bar, style);
            }
            updateProgress(bar, living);
            bar.setName(living.getDisplayName());
        } else {
            hideBar(living);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        removeBar(event.getEntity());
    }

    @SubscribeEvent
    public static void onLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        removeBar(event.getEntity());
    }

    private static void addTrackingPlayers(LivingEntity living, ServerBossEvent bar, BossBarStyle style) {
        if (!(living.level() instanceof ServerLevel level)) {
            return;
        }
        Set<UUID> trackers = TRACKERS.get(living.getUUID());
        if (trackers == null) {
            return;
        }
        for (UUID playerId : List.copyOf(trackers)) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
            if (player == null) {
                continue;
            }
            bar.addPlayer(player);
            PacketDistributor.sendToPlayer(player, BossBarColorPayload.set(bar.getId(), style.colorRgb()));
        }
    }

    private static ServerBossEvent getOrCreate(LivingEntity living, BossBarStyle style) {
        return BARS.computeIfAbsent(living.getUUID(), id -> {
            ServerBossEvent bar = new ServerBossEvent(
                    living.getDisplayName(),
                    BossEvent.BossBarColor.WHITE,
                    style.overlay()
            );
            updateProgress(bar, living);
            return bar;
        });
    }

    private static void updateProgress(ServerBossEvent bar, LivingEntity living) {
        float max = living.getMaxHealth();
        bar.setProgress(max <= 0.0F ? 0.0F : Mth.clamp(living.getHealth() / max, 0.0F, 1.0F));
    }

    private static void hideBar(LivingEntity living) {
        dismissBar(living.getUUID());
    }

    private static void removeBar(net.minecraft.world.entity.Entity entity) {
        TRACKERS.remove(entity.getUUID());
        dismissBar(entity.getUUID());
    }

    private static void dismissBar(UUID entityId) {
        ServerBossEvent bar = BARS.remove(entityId);
        if (bar == null) {
            return;
        }
        for (ServerPlayer player : List.copyOf(bar.getPlayers())) {
            PacketDistributor.sendToPlayer(player, BossBarColorPayload.clear(bar.getId()));
        }
        bar.removeAllPlayers();
    }
}
