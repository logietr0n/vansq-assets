package com.vansqmod.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vansqmod.VansqMod;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Hooks Reactive Music's sound-ID ducking so vansq boss tracks fade RM out.
 * Matching is a substring on the sound event <em>path</em> (namespace ignored),
 * e.g. {@code vansqmod:music.boss.missioner} matches {@code music.boss}.
 */
public final class ReactiveMusicMuteCompat {

    public static final String BOSS_MUSIC_PATH_PREFIX = "music.boss";

    private static final Path CONFIG_PATH = Path.of("config/ReactiveMusic.json5");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private ReactiveMusicMuteCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded("reactivemusic");
    }

    /**
     * Adds {@link #BOSS_MUSIC_PATH_PREFIX} to {@code soundsMuteMusicIgnoreDistance}
     * in memory and on disk so looping boss tracks duck RM from anywhere.
     */
    public static void ensureBossMusicMuteEntry() {
        if (!isLoaded()) {
            return;
        }
        boolean liveChanged = addToLiveList();
        boolean fileChanged = addToConfigFile();
        if (liveChanged || fileChanged) {
            VansqMod.LOGGER.info("Reactive Music will duck for sound paths containing '{}'", BOSS_MUSIC_PATH_PREFIX);
        }
    }

    /**
     * Explicitly tracks a playing sound for RM ducking. Useful if the config list
     * was empty at RM startup; {@code ignoreDistance} matches global/player-relative music.
     */
    public static void trackPlayingSound(@Nullable SoundInstance instance, boolean ignoreDistance) {
        if (instance == null || !isLoaded()) {
            return;
        }
        try {
            Class<?> rm = Class.forName("circuitlord.reactivemusic.ReactiveMusic");
            Method track = rm.getMethod("trackSoundMuteMusic", SoundInstance.class, boolean.class);
            track.invoke(null, instance, ignoreDistance);
        } catch (ReflectiveOperationException e) {
            VansqMod.LOGGER.debug("Reactive Music mute hook unavailable: {}", e.toString());
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean addToLiveList() {
        try {
            Class<?> rm = Class.forName("circuitlord.reactivemusic.ReactiveMusic");
            Object config = rm.getField("config").get(null);
            if (config == null) {
                return false;
            }
            Field field = config.getClass().getField("soundsMuteMusicIgnoreDistance");
            Object raw = field.get(config);
            if (!(raw instanceof List<?> list)) {
                return false;
            }
            List<String> strings = (List<String>) list;
            if (containsPrefix(strings)) {
                return false;
            }
            strings.add(BOSS_MUSIC_PATH_PREFIX);
            try {
                Method save = config.getClass().getMethod("saveConfig");
                save.invoke(null);
            } catch (ReflectiveOperationException ignored) {
                // File write below still persists the entry.
            }
            return true;
        } catch (ReflectiveOperationException | ClassCastException e) {
            VansqMod.LOGGER.debug("Could not update live Reactive Music mute list: {}", e.toString());
            return false;
        }
    }

    private static boolean addToConfigFile() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                return false;
            }
            JsonObject root;
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                root = JsonParser.parseReader(reader).getAsJsonObject();
            }
            JsonArray array = root.getAsJsonArray("soundsMuteMusicIgnoreDistance");
            if (array == null) {
                array = new JsonArray();
                root.add("soundsMuteMusicIgnoreDistance", array);
            }
            for (JsonElement element : array) {
                if (element.isJsonPrimitive() && BOSS_MUSIC_PATH_PREFIX.equals(element.getAsString())) {
                    return false;
                }
            }
            array.add(BOSS_MUSIC_PATH_PREFIX);
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
            return true;
        } catch (Exception e) {
            VansqMod.LOGGER.warn("Could not write Reactive Music boss mute entry: {}", e.toString());
            return false;
        }
    }

    private static boolean containsPrefix(List<String> values) {
        for (String value : values) {
            if (BOSS_MUSIC_PATH_PREFIX.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
