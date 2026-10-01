package com.vansqmod.client;

import com.vansqmod.VansqMod;
import com.vansqmod.compat.AstronomyWorldData;
import com.vansqmod.network.AstronomySpacePayload;
import com.vansqmod.network.AstronomySpaceUpdatePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

/**
 * Applies the world's Spyglass Astronomy snapshot on this client using Astronomy's
 * own {@code SpaceDataManager} save/load, then uploads local edits so every player
 * sees the same sky.
 */
@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class AstronomyClientSync {

    public static final int DEFAULT_STAR_COUNT = 2048;
    private static final int OLD_DEFAULT_STAR_COUNT = 1024;
    private static final String ASTRONOMY_MOD = "spyglass_astronomy";
    private static final String CLIENT_CLASS = "com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient";
    private static final String FORMAT_MARK = "Spyglass Astronomy - Format:";

    private static String pending;
    private static String pendingUpload;
    private static String lastApplied = "";
    private static boolean applying;
    private static boolean countMigrated;

    private AstronomyClientSync() {
    }

    public static void handleSnapshot(String snapshot) {
        if (!isUsable(snapshot)) {
            return;
        }
        if (!spaceReady()) {
            pending = snapshot;
            return;
        }
        apply(snapshot);
    }

    public static void onSpaceLoaded() {
        if (!ModList.get().isLoaded(ASTRONOMY_MOD)) {
            return;
        }
        try {
            if (pending != null) {
                String snapshot = pending;
                pending = null;
                apply(snapshot);
                return;
            }
            MinecraftServer integrated = Minecraft.getInstance().getSingleplayerServer();
            if (integrated == null) {
                return;
            }
            AstronomyWorldData data = AstronomyWorldData.get(integrated);
            String local = serialize();
            lastApplied = local;
            if (data.hasSnapshot()) {
                if (!data.snapshot().equals(local)) {
                    apply(data.snapshot());
                }
                return;
            }
            if (isUsable(local)) {
                sendOrQueue(local);
            }
        } finally {
            flushCountMigration();
        }
    }

    public static void onLocalChange() {
        if (applying || !ModList.get().isLoaded(ASTRONOMY_MOD)) {
            return;
        }
        String snapshot = serialize();
        if (!isUsable(snapshot) || snapshot.equals(lastApplied)) {
            return;
        }
        lastApplied = snapshot;
        sendOrQueue(snapshot);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        flushPendingUpload();
    }

    private static void apply(String snapshot) {
        if (!isUsable(snapshot) || snapshot.equals(lastApplied)) {
            return;
        }
        Object manager = spaceDataManager();
        if (manager == null) {
            pending = snapshot;
            return;
        }
        applying = true;
        try {
            File file = dataFile(manager);
            if (file == null) {
                pending = snapshot;
                return;
            }
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            Files.writeString(file.toPath(), snapshot, Charset.defaultCharset());
            clearList("stars");
            clearList("constellations");
            clearList("orbitingBodies");
            manager.getClass().getMethod("loadData").invoke(manager);
            Class<?> client = Class.forName(CLIENT_CLASS);
            client.getMethod("generateSpace", boolean.class).invoke(null, false);
            client.getMethod("updateKnowledge").invoke(null);
            Object renderer = client.getField("spaceRenderingManager").get(null);
            if (renderer != null) {
                renderer.getClass().getMethod("scheduleConstellationsUpdate").invoke(renderer);
            }
            lastApplied = snapshot;
        } catch (Exception e) {
            VansqMod.LOGGER.warn("Failed to apply Spyglass Astronomy world snapshot", e);
        } finally {
            applying = false;
            flushCountMigration();
        }
    }

    public static void ensureDoubledDefaultStarCount() {
        try {
            Class<?> client = Class.forName(CLIENT_CLASS);
            int count = (Integer) client.getMethod("getStarCount").invoke(null);
            if (count != OLD_DEFAULT_STAR_COUNT) {
                return;
            }
            client.getMethod("setStarCount", int.class).invoke(null, DEFAULT_STAR_COUNT);
            countMigrated = true;
        } catch (Exception ignored) {
        }
    }

    private static void flushCountMigration() {
        if (!countMigrated) {
            return;
        }
        countMigrated = false;
        onLocalChange();
    }

    private static String serialize() {
        Object manager = spaceDataManager();
        if (manager == null) {
            return "";
        }
        try {
            Field changes = manager.getClass().getDeclaredField("changesMade");
            changes.setAccessible(true);
            if (changes.getInt(manager) <= 0) {
                changes.setInt(manager, 1);
            }
            manager.getClass().getMethod("saveData").invoke(manager);
            File file = dataFile(manager);
            if (file == null || !file.isFile()) {
                return "";
            }
            return Files.readString(file.toPath(), Charset.defaultCharset());
        } catch (Exception e) {
            VansqMod.LOGGER.warn("Failed to serialize Spyglass Astronomy snapshot", e);
            return "";
        }
    }

    private static boolean spaceReady() {
        return spaceDataManager() != null;
    }

    private static Object spaceDataManager() {
        try {
            return Class.forName(CLIENT_CLASS).getField("spaceDataManager").get(null);
        } catch (Exception e) {
            return null;
        }
    }

    private static File dataFile(Object manager) {
        try {
            Field data = manager.getClass().getDeclaredField("data");
            data.setAccessible(true);
            Object file = data.get(manager);
            return file instanceof File f ? f : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static void clearList(String fieldName) {
        try {
            Object list = Class.forName(CLIENT_CLASS).getField(fieldName).get(null);
            if (list instanceof List<?> values) {
                values.clear();
            }
        } catch (Exception ignored) {
        }
    }

    private static void sendOrQueue(String snapshot) {
        if (!isUsable(snapshot)) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null || client.player == null) {
            pendingUpload = snapshot;
            return;
        }
        pendingUpload = null;
        PacketDistributor.sendToServer(new AstronomySpaceUpdatePayload(snapshot));
    }

    private static void flushPendingUpload() {
        if (pendingUpload == null) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() == null || client.player == null) {
            return;
        }
        String snapshot = pendingUpload;
        pendingUpload = null;
        PacketDistributor.sendToServer(new AstronomySpaceUpdatePayload(snapshot));
    }

    private static boolean isUsable(String snapshot) {
        return snapshot != null
                && snapshot.contains(FORMAT_MARK)
                && snapshot.length() <= AstronomySpacePayload.MAX_LENGTH;
    }
}
