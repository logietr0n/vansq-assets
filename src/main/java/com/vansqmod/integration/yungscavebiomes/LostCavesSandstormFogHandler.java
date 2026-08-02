package com.vansqmod.integration.yungscavebiomes;

import com.vansqmod.VansqMod;
import com.vansqmod.network.SandstormVanillaFogPayload;
import com.yungnickyoung.minecraft.yungscavebiomes.sandstorm.ISandstormServerDataProvider;
import com.yungnickyoung.minecraft.yungscavebiomes.sandstorm.SandstormServerData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Re-enables vanilla fog (overriding Distant Horizons' disable) shortly before and
 * during YUNG's Cave Biomes sandstorms while the player is in Lost Caves.
 * <p>
 * Re-evaluates continuously (not only when a storm starts) so walking into an
 * already-active sandstorm still enables fog. Heartbeats only while fog is needed.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class LostCavesSandstormFogHandler {

    private static final ResourceKey<Biome> LOST_CAVES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "lost_caves")
    );

    /** Enable vanilla fog this many ticks before the sandstorm cooldown hits 0. */
    private static final int WARNING_TICKS = 5 * 20;

    /** How often to evaluate fog need. */
    private static final int CHECK_INTERVAL = 5;

    /** Resend enable while fog is needed so mid-storm entry cannot miss a packet. */
    private static final int HEARTBEAT_INTERVAL = 40;

    private static final Map<UUID, Boolean> LAST_SENT = new HashMap<>();

    private static Field cooldownTicksField;
    private static boolean cooldownFieldResolved;

    private LostCavesSandstormFogHandler() {
    }

    public static boolean isEnabled() {
        return ModList.get().isLoaded("yungscavebiomes");
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!isEnabled()) {
            return;
        }
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer) || player.level().isClientSide()) {
            return;
        }
        if (serverPlayer.tickCount % CHECK_INTERVAL != 0) {
            return;
        }

        try {
            boolean needFog = shouldEnableVanillaFog(serverPlayer);
            Boolean previous = LAST_SENT.put(serverPlayer.getUUID(), needFog);
            boolean changed = previous == null || previous != needFog;
            // Heartbeat only while enabling — avoids needless disable spam with dense fog packs.
            boolean heartbeat = needFog && serverPlayer.tickCount % HEARTBEAT_INTERVAL == 0;
            if (changed || heartbeat) {
                PacketDistributor.sendToPlayer(serverPlayer, new SandstormVanillaFogPayload(needFog));
            }
        } catch (Throwable t) {
            VansqMod.LOGGER.debug("Lost Caves sandstorm fog check failed", t);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        LAST_SENT.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        LAST_SENT.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_SENT.remove(event.getEntity().getUUID());
    }

    private static boolean shouldEnableVanillaFog(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (!(level instanceof ISandstormServerDataProvider provider)) {
            return false;
        }
        if (!level.getBiome(player.blockPosition()).is(LOST_CAVES)) {
            return false;
        }

        SandstormServerData data = provider.getSandstormServerData();
        if (data.isSandstormActive()) {
            return true;
        }

        int cooldown = readCooldownTicks(data);
        return cooldown > 0 && cooldown <= WARNING_TICKS;
    }

    private static int readCooldownTicks(SandstormServerData data) {
        try {
            Field field = cooldownTicksField();
            if (field == null) {
                return -1;
            }
            return field.getInt(data);
        } catch (ReflectiveOperationException ignored) {
            return -1;
        }
    }

    private static Field cooldownTicksField() {
        if (cooldownFieldResolved) {
            return cooldownTicksField;
        }
        cooldownFieldResolved = true;
        try {
            Field field = SandstormServerData.class.getDeclaredField("cooldownTicks");
            field.setAccessible(true);
            cooldownTicksField = field;
        } catch (ReflectiveOperationException ignored) {
            cooldownTicksField = null;
        }
        return cooldownTicksField;
    }
}
