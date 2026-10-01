package com.vansqmod.compat;

import com.vansqmod.network.HomeBedPayload;
import net.mehvahdjukaar.sleep_tight.STPlatStuff;
import net.mehvahdjukaar.sleep_tight.core.BedData;
import net.mehvahdjukaar.sleep_tight.core.ModEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * HOME follows the Sleep Tight bed where this player has the highest bed level.
 * Levels live on each bed, so beds are recorded when the player sleeps there
 * and refreshed when their chunk is loaded.
 */
public final class SleepTightHomeBedSync {

    private static final String BEDS_KEY = "vansqmod_home_beds";

    private SleepTightHomeBedSync() {
    }

    public static void sendKnownBed(ServerPlayer player) {
        List<KnownBed> beds = load(player);
        refreshLoaded(player, beds);
        observeRespawn(player, beds);
        save(player, beds);
        sendBest(player, beds);
    }

    public static void sendSleptBed(ServerPlayer player) {
        player.getSleepingPos().ifPresent(pos -> {
            List<KnownBed> beds = load(player);
            if (observe(player, player.level(), pos, beds)) {
                save(player, beds);
                sendBest(player, beds);
            }
        });
    }

    private static void observeRespawn(ServerPlayer player, List<KnownBed> beds) {
        BlockPos respawn = player.getRespawnPosition();
        ResourceKey<Level> dimension = player.getRespawnDimension();
        if (respawn == null || dimension == null) {
            return;
        }
        ServerLevel level = player.server.getLevel(dimension);
        if (level != null && level.hasChunkAt(respawn)) {
            observe(player, level, respawn, beds);
        }
    }

    private static boolean observe(ServerPlayer player, Level level, BlockPos pos, List<KnownBed> beds) {
        BedData bed = STPlatStuff.getBedDataIfPresent(level, pos);
        if (bed == null) {
            return false;
        }
        BlockPos head = ModEvents.getBedHead(level.getBlockState(pos), pos);
        if (head == null) {
            head = pos;
        }
        int levelValue = bed.getBedLevel(player) & 0xFF;
        long seen = level.getGameTime();
        UUID id = bed.getId();
        String dimension = level.dimension().location().toString();
        for (int i = 0; i < beds.size(); i++) {
            KnownBed known = beds.get(i);
            if (id.equals(known.id)) {
                beds.set(i, new KnownBed(id, dimension, head, levelValue, seen));
                return true;
            }
        }
        beds.add(new KnownBed(id, dimension, head, levelValue, seen));
        return true;
    }

    private static void refreshLoaded(ServerPlayer player, List<KnownBed> beds) {
        for (int i = beds.size() - 1; i >= 0; i--) {
            KnownBed known = beds.get(i);
            ResourceKey<Level> dimension = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, net.minecraft.resources.ResourceLocation.parse(known.dimension));
            ServerLevel level = player.server.getLevel(dimension);
            if (level == null || !level.hasChunkAt(known.pos)) {
                continue;
            }
            BedData bed = STPlatStuff.getBedDataIfPresent(level, known.pos);
            if (bed == null || !known.id.equals(bed.getId())) {
                beds.remove(i);
                continue;
            }
            BlockPos head = ModEvents.getBedHead(level.getBlockState(known.pos), known.pos);
            beds.set(i, new KnownBed(
                    known.id,
                    known.dimension,
                    head == null ? known.pos : head,
                    bed.getBedLevel(player) & 0xFF,
                    known.seen
            ));
        }
    }

    private static void sendBest(ServerPlayer player, List<KnownBed> beds) {
        KnownBed best = null;
        for (KnownBed bed : beds) {
            if (best == null || bed.level > best.level || (bed.level == best.level && bed.seen > best.seen)) {
                best = bed;
            }
        }
        if (best == null) {
            PacketDistributor.sendToPlayer(player, HomeBedPayload.clear());
            return;
        }
        PacketDistributor.sendToPlayer(player, new HomeBedPayload(
                true,
                best.dimension,
                best.pos.getX(),
                best.pos.getY(),
                best.pos.getZ()
        ));
    }

    private static List<KnownBed> load(ServerPlayer player) {
        List<KnownBed> beds = new ArrayList<>();
        ListTag list = player.getPersistentData().getList(BEDS_KEY, Tag.TAG_COMPOUND);
        for (Tag tag : list) {
            if (!(tag instanceof CompoundTag compound) || !compound.hasUUID("id")) {
                continue;
            }
            beds.add(new KnownBed(
                    compound.getUUID("id"),
                    compound.getString("dim"),
                    new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z")),
                    compound.getInt("level"),
                    compound.getLong("seen")
            ));
        }
        return beds;
    }

    private static void save(ServerPlayer player, List<KnownBed> beds) {
        ListTag list = new ListTag();
        for (KnownBed bed : beds) {
            CompoundTag compound = new CompoundTag();
            compound.putUUID("id", bed.id);
            compound.putString("dim", bed.dimension);
            compound.putInt("x", bed.pos.getX());
            compound.putInt("y", bed.pos.getY());
            compound.putInt("z", bed.pos.getZ());
            compound.putInt("level", bed.level);
            compound.putLong("seen", bed.seen);
            list.add(compound);
        }
        player.getPersistentData().put(BEDS_KEY, list);
    }

    private record KnownBed(UUID id, String dimension, BlockPos pos, int level, long seen) {
    }
}
