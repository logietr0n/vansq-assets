package com.vansqmod.compat;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * World-wide flag: a player has entered the Nether at least once.
 */
public final class NetherEnchantUnlock extends SavedData {

    private static final String STORAGE_ID = "vansqmod_nether_enchant";
    private static final String NBT_VISITED = "visited";

    private boolean visited;

    public NetherEnchantUnlock() {
    }

    public static NetherEnchantUnlock load(CompoundTag tag, HolderLookup.Provider registries) {
        NetherEnchantUnlock data = new NetherEnchantUnlock();
        data.visited = tag.getBoolean(NBT_VISITED);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean(NBT_VISITED, this.visited);
        return tag;
    }

    public boolean visited() {
        return this.visited;
    }

    public void unlock() {
        if (this.visited) {
            return;
        }
        this.visited = true;
        this.setDirty();
    }

    public static boolean isUnlocked(MinecraftServer server) {
        return get(server).visited();
    }

    public static void unlock(MinecraftServer server) {
        get(server).unlock();
    }

    private static NetherEnchantUnlock get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return new NetherEnchantUnlock();
        }
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(NetherEnchantUnlock::new, NetherEnchantUnlock::load, null),
                STORAGE_ID
        );
    }
}
