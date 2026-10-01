package com.vansqmod.compat;

import com.vansqmod.mixin.BiomeManagerSeedAccessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * World-owned Spyglass Astronomy snapshot (seeds, constellations, names).
 * Uses Astronomy's own text save format so every player can apply the same sky.
 */
public final class AstronomyWorldData extends SavedData {

    public static final int DEFAULT_STAR_COUNT = 2048;
    public static final float DEFAULT_YEAR_LENGTH = 8.0F;
    private static final String STORAGE_ID = "vansqmod_spyglass_astronomy";
    private static final String NBT_SNAPSHOT = "snapshot";

    private String snapshot = "";

    public AstronomyWorldData() {
    }

    public static AstronomyWorldData load(CompoundTag tag, HolderLookup.Provider registries) {
        AstronomyWorldData data = new AstronomyWorldData();
        data.snapshot = tag.getString(NBT_SNAPSHOT);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString(NBT_SNAPSHOT, this.snapshot == null ? "" : this.snapshot);
        return tag;
    }

    public boolean hasSnapshot() {
        return this.snapshot != null && !this.snapshot.isBlank();
    }

    public String snapshot() {
        return this.snapshot == null ? "" : this.snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot == null ? "" : snapshot;
        this.setDirty();
    }

    public void ensureDefault(long biomeSeed) {
        if (hasSnapshot()) {
            return;
        }
        setSnapshot(defaultSnapshot(biomeSeed));
    }

    public static String defaultSnapshot(long biomeSeed) {
        return "Spyglass Astronomy - Format: 1\n---\n"
                + biomeSeed
                + "\n---\n---\n---\n---\n"
                + DEFAULT_STAR_COUNT
                + " "
                + DEFAULT_YEAR_LENGTH
                + "\n---";
    }

    public static AstronomyWorldData get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return new AstronomyWorldData();
        }
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(AstronomyWorldData::new, AstronomyWorldData::load, null),
                STORAGE_ID
        );
    }

    public static long biomeSeed(Level level) {
        if (level == null) {
            return 0L;
        }
        BiomeManager manager = level.getBiomeManager();
        if (manager instanceof BiomeManagerSeedAccessor accessor) {
            return accessor.vansqmod$getBiomeZoomSeed();
        }
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getSeed();
        }
        return 0L;
    }
}
