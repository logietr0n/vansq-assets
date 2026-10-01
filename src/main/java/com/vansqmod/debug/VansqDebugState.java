package com.vansqmod.debug;

import com.vansqmod.VansqMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the on/off state for {@code /vansq debug} spawn overrides.
 *
 * <p>{@code /vansq debug equip}: only the yes/no "does this mob get equipment at
 * all" gates are forced to succeed: vanilla armor via the first roll in
 * {@code Mob#populateDefaultEquipmentSlots}, zombie-family weapons via
 * {@code MobSpawnWeapons}, and Putrid's fishing-rod / copper-lantern rolls.
 * Relative choice rolls (armor tier upgrades, piece-count, which weapon) stay
 * on the real random so original ratios among equipped spawns are unchanged.</p>
 *
 * <p>{@code /vansq debug mobenchant}: only EnchantWithMob's natural-spawn
 * yes/no chance is forced. Mobs it would never enchant (config exclusions,
 * animals unless that config is on, breeding / conversion / structure /
 * summoned) stay unenchanted. Which enchantments they receive still follows
 * EnchantWithMob's difficulty budget.</p>
 *
 * <p>{@code /vansq debug chaos}: forces rare yes/no rolls to succeed: zombie-family
 * baby odds, skeleton/mellowed baby pairs, omen/follow goat wakes, rare chicken
 * breeding, charged creeper spawns, Gelid ice-snowball holds, and tweed-from-straw.
 * Relative choice rolls stay random. Natural EnchantWithMob yes/no chance is also
 * multiplied by 4 (still capped at 100%). {@code /vansq debug mobenchant} remains
 * a separate 100% force.</p>
 *
 * <p>{@code /vansq debug playercount}: extra fake players at the command user's
 * position for proximity tests (boss health, etc.). They are not entities.</p>
 *
 * <p>Volatile because command execution (server command thread) and mob spawning
 * (server tick thread) are both effectively "the server thread" in vanilla/NeoForge,
 * but volatile costs nothing and removes any doubt if that ever changes.</p>
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class VansqDebugState {

    private static final Map<UUID, Integer> FAKE_PLAYERS = new ConcurrentHashMap<>();

    private static volatile boolean forceEquipmentEnabled = false;
    private static volatile boolean forceMobEnchantEnabled = false;
    private static volatile boolean forceRareEventsEnabled = false;

    private VansqDebugState() {
    }

    public static boolean isForceEquipmentEnabled() {
        return forceEquipmentEnabled;
    }

    public static void setForceEquipmentEnabled(boolean enabled) {
        forceEquipmentEnabled = enabled;
    }

    public static boolean isForceMobEnchantEnabled() {
        return forceMobEnchantEnabled;
    }

    public static void setForceMobEnchantEnabled(boolean enabled) {
        forceMobEnchantEnabled = enabled;
    }

    public static boolean isForceRareEventsEnabled() {
        return forceRareEventsEnabled;
    }

    public static boolean isChaosEnabled() {
        return forceRareEventsEnabled;
    }

    public static void setForceRareEventsEnabled(boolean enabled) {
        forceRareEventsEnabled = enabled;
    }

    public static float chaosEnchantMultiplier() {
        return forceRareEventsEnabled ? 4.0F : 1.0F;
    }

    public static boolean rareEventSucceeds(RandomSource random, float chance) {
        return forceRareEventsEnabled || random.nextFloat() < chance;
    }

    public static boolean rareEventSucceeds(RandomSource random, int oneIn) {
        return forceRareEventsEnabled || random.nextInt(oneIn) == 0;
    }

    public static int getFakePlayerCount(UUID playerId) {
        return FAKE_PLAYERS.getOrDefault(playerId, 0);
    }

    public static void setFakePlayerCount(UUID playerId, int count) {
        if (count <= 0) {
            FAKE_PLAYERS.remove(playerId);
        } else {
            FAKE_PLAYERS.put(playerId, count);
        }
    }

    public static int extraFakePlayersIn(Level level) {
        int extra = 0;
        for (Player player : level.players()) {
            extra += getFakePlayerCount(player.getUUID());
        }
        return extra;
    }

    public static int extraFakePlayersNear(Level level, Vec3 pos, double range) {
        int extra = 0;
        for (Player player : level.players()) {
            if (pos.distanceTo(player.position()) <= range) {
                extra += getFakePlayerCount(player.getUUID());
            }
        }
        return extra;
    }

    public static int extraFakePlayersAmong(Iterable<?> nearbyPlayers) {
        int extra = 0;
        for (Object object : nearbyPlayers) {
            if (object instanceof Entity entity) {
                extra += getFakePlayerCount(entity.getUUID());
            }
        }
        return extra;
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FAKE_PLAYERS.remove(player.getUUID());
        }
    }
}
