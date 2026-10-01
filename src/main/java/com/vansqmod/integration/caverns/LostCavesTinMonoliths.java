package com.vansqmod.integration.caverns;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * C&amp;C tin monoliths sample biome at Y 64 and skip a 2048-block square around spawn.
 * Lost Caves almost never exists at sea level, so the extra structure set never places.
 * Sample typical cave heights instead and skip the spawn ban when Lost Caves is present.
 */
public final class LostCavesTinMonoliths {

    private static final ResourceKey<Biome> LOST_CAVES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "lost_caves")
    );
    /** Every 8 blocks from Y 48 down to -56, covering typical Lost Caves. */
    private static final int[] SAMPLE_YS;

    static {
        int minY = -56;
        int maxY = 48;
        int step = 8;
        SAMPLE_YS = new int[(maxY - minY) / step + 1];
        for (int i = 0, y = maxY; y >= minY; y -= step, i++) {
            SAMPLE_YS[i] = y;
        }
    }

    private LostCavesTinMonoliths() {
    }

    /** True for the Lost Caves-only extra set, not the default Overworld C&C monoliths. */
    public static boolean isLostCavesOnlyStructure(Structure structure) {
        HolderSet<Biome> biomes = structure.biomes();
        boolean any = false;
        for (Holder<Biome> biome : biomes) {
            any = true;
            if (!biome.is(LOST_CAVES)) {
                return false;
            }
        }
        return any;
    }

    public static int findLostCavesY(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMinBlockX() + 8;
        int z = chunk.getMinBlockZ() + 8;
        var sampler = context.randomState().sampler();
        var biomes = context.biomeSource();
        for (int y : SAMPLE_YS) {
            Holder<Biome> biome = biomes.getNoiseBiome(
                    QuartPos.fromBlock(x),
                    QuartPos.fromBlock(y),
                    QuartPos.fromBlock(z),
                    sampler
            );
            if (biome.is(LOST_CAVES)) {
                return y;
            }
        }
        return Integer.MIN_VALUE;
    }
}
