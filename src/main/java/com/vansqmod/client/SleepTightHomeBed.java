package com.vansqmod.client;

import circuitlord.reactivemusic.SongPicker;
import circuitlord.reactivemusic.SongpackEventType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

/**
 * Reactive Music's HOME event is a 64-block radius around the Sleep Tight bed
 * where this player has the highest bed level.
 */
public final class SleepTightHomeBed {

    private static final double HOME_RADIUS_SQR = 64.0 * 64.0;

    private static boolean present;
    private static ResourceLocation dimension;
    private static BlockPos pos;

    private SleepTightHomeBed() {
    }

    public static void accept(boolean hasBed, String dimensionId, int x, int y, int z) {
        if (!hasBed || dimensionId == null || dimensionId.isEmpty()) {
            present = false;
            dimension = null;
            pos = null;
            return;
        }
        present = true;
        dimension = ResourceLocation.parse(dimensionId);
        pos = new BlockPos(x, y, z);
    }

    public static void overwriteHomeEvent() {
        if (!ModList.get().isLoaded("sleep_tight")) {
            return;
        }
        SongPicker.songpackEventMap.put(SongpackEventType.HOME, nearHomeBed());
    }

    private static boolean nearHomeBed() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.level;
        if (player == null || level == null || !present || pos == null || dimension == null) {
            return false;
        }
        if (!level.dimension().location().equals(dimension)) {
            return false;
        }
        return player.position().distanceToSqr(pos.getCenter()) < HOME_RADIUS_SQR;
    }
}
