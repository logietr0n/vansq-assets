package com.vansqmod.integration.yungscavebiomes;

import com.vansqmod.VansqMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ice sheets placed during worldgen do not run a neighbor update, so they stay unlit
 * on ores. Each ice-sheet chunk is ore-checked and fixed once after load.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class IceSheetGlowTicker {

    private static final int CHUNKS_PER_TICK = 2;
    private static final int QUEUE_LOOKUPS_PER_TICK = 48;
    private static final int SET_BLOCKS_PER_TICK = 16;
    /** Keep the server tick free to finish incoming chunks. */
    private static final long TICK_BUDGET_NANOS = 2_000_000L;

    private static final ConcurrentHashMap<ServerLevel, ConcurrentHashMap<Long, Boolean>> PENDING =
            new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<ServerLevel, ConcurrentHashMap<Long, Boolean>> SCANNED =
            new ConcurrentHashMap<>();

    private IceSheetGlowTicker() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || level.dimension() != Level.OVERWORLD
                || !(event.getChunk() instanceof LevelChunk chunk)
                || !ModList.get().isLoaded("yungscavebiomes")) {
            return;
        }
        ChunkPos pos = chunk.getPos();
        if (wasScanned(level, pos.toLong()) || !IceSheetGlowHelper.mayHaveIceSheet(chunk)) {
            return;
        }
        enqueue(level, pos);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            PENDING.remove(level);
            SCANNED.remove(level);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || level.dimension() != Level.OVERWORLD
                || !ModList.get().isLoaded("yungscavebiomes")
                || level.players().isEmpty()) {
            return;
        }
        drainQueue(level);
    }

    private static void enqueue(ServerLevel level, ChunkPos pos) {
        long key = pos.toLong();
        if (wasScanned(level, key)) {
            return;
        }
        PENDING.computeIfAbsent(level, unused -> new ConcurrentHashMap<>())
                .putIfAbsent(key, Boolean.TRUE);
    }

    private static boolean wasScanned(ServerLevel level, long key) {
        ConcurrentHashMap<Long, Boolean> scanned = SCANNED.get(level);
        return scanned != null && scanned.containsKey(key);
    }

    private static void markScanned(ServerLevel level, long key) {
        SCANNED.computeIfAbsent(level, unused -> new ConcurrentHashMap<>())
                .put(key, Boolean.TRUE);
    }

    private static void drainQueue(ServerLevel level) {
        ConcurrentHashMap<Long, Boolean> pending = PENDING.get(level);
        if (pending == null || pending.isEmpty()) {
            return;
        }
        int iceBudget = CHUNKS_PER_TICK;
        int lookups = QUEUE_LOOKUPS_PER_TICK;
        long deadline = System.nanoTime() + TICK_BUDGET_NANOS;
        Iterator<Long> iterator = pending.keySet().iterator();
        while (iceBudget > 0 && lookups > 0 && iterator.hasNext()) {
            if (System.nanoTime() >= deadline) {
                break;
            }
            lookups--;
            long key = iterator.next();
            iterator.remove();
            if (wasScanned(level, key)) {
                continue;
            }
            ChunkPos pos = new ChunkPos(key);
            LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
            if (chunk == null) {
                continue;
            }
            if (!IceSheetGlowHelper.mayHaveIceSheet(chunk)
                    || !IceSheetGlowHelper.mayHaveNearbyGlowOre(level, chunk)) {
                markScanned(level, key);
                continue;
            }
            if (!IceSheetGlowHelper.fixChunk(level, chunk, SET_BLOCKS_PER_TICK)) {
                pending.put(key, Boolean.TRUE);
                return;
            }
            markScanned(level, key);
            iceBudget--;
        }
    }
}
