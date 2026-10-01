package com.vansqmod.client;

import com.vansqmod.VansqMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vorbis granule duration for streamed boss tracks. Used to start the loop
 * before the intro's last samples so the stream open does not insert a gap.
 */
final class BossMusicOggDurations {

    private static final Map<ResourceLocation, Long> CACHE = new ConcurrentHashMap<>();
    private static final byte[] OGGS = "OggS".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] VORBIS = "vorbis".getBytes(StandardCharsets.US_ASCII);

    private BossMusicOggDurations() {
    }

    static long nanos(SoundInstance instance) {
        if (instance == null) {
            return -1L;
        }
        Sound sound = instance.getSound();
        if (sound == null || sound == SoundManager.EMPTY_SOUND || sound == SoundManager.INTENTIONALLY_EMPTY_SOUND) {
            return -1L;
        }
        return nanos(sound.getPath());
    }

    static long nanos(ResourceLocation oggPath) {
        if (oggPath == null) {
            return -1L;
        }
        return CACHE.computeIfAbsent(oggPath, BossMusicOggDurations::readNanos);
    }

    private static long readNanos(ResourceLocation oggPath) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return -1L;
        }
        try {
            Optional<Resource> resource = mc.getResourceManager().getResource(oggPath);
            if (resource.isEmpty()) {
                return -1L;
            }
            try (InputStream in = resource.get().open()) {
                return parse(in.readAllBytes());
            }
        } catch (IOException e) {
            VansqMod.LOGGER.debug("Could not read boss music duration for {}: {}", oggPath, e.toString());
            return -1L;
        }
    }

    private static long parse(byte[] data) {
        int sampleRate = 0;
        long lastGranule = 0L;
        int pos = 0;
        while (pos + 27 < data.length) {
            if (!match(data, pos, OGGS)) {
                pos++;
                continue;
            }
            long granule = longLe(data, pos + 6);
            int segments = data[pos + 26] & 0xFF;
            int table = pos + 27;
            if (table + segments > data.length) {
                break;
            }
            int payloadBytes = 0;
            for (int i = 0; i < segments; i++) {
                payloadBytes += data[table + i] & 0xFF;
            }
            int payload = table + segments;
            if (payload + payloadBytes > data.length) {
                break;
            }
            if (sampleRate <= 0 && payload + 16 <= data.length
                    && data[payload] == 1 && match(data, payload + 1, VORBIS)) {
                sampleRate = intLe(data, payload + 12);
            }
            if (granule >= 0L) {
                lastGranule = granule;
            }
            pos = payload + payloadBytes;
        }
        if (sampleRate <= 0 || lastGranule <= 0L) {
            return -1L;
        }
        return lastGranule * 1_000_000_000L / sampleRate;
    }

    private static boolean match(byte[] data, int offset, byte[] expected) {
        if (offset < 0 || offset + expected.length > data.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (data[offset + i] != expected[i]) {
                return false;
            }
        }
        return true;
    }

    private static int intLe(byte[] data, int offset) {
        return (data[offset] & 0xFF)
                | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 3] & 0xFF) << 24);
    }

    private static long longLe(byte[] data, int offset) {
        return (intLe(data, offset) & 0xFFFFFFFFL)
                | ((long) intLe(data, offset + 4) << 32);
    }
}
