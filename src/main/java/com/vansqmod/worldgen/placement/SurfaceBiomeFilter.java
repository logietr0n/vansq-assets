package com.vansqmod.worldgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vansqmod.registry.ModPlacementModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * Like {@code minecraft:biome}, but checks the climate biome at the top of the world
 * instead of the 3D biome at the ore position. Cave biomes (lush, dripstone, etc.)
 * therefore inherit gold/silver rules from the surface column.
 */
public class SurfaceBiomeFilter extends PlacementFilter {

    public static final MapCodec<SurfaceBiomeFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(filter -> filter.biomes),
            Codec.BOOL.optionalFieldOf("negate", false).forGetter(filter -> filter.negate)
    ).apply(instance, SurfaceBiomeFilter::new));

    private final HolderSet<Biome> biomes;
    private final boolean negate;

    public SurfaceBiomeFilter(HolderSet<Biome> biomes, boolean negate) {
        this.biomes = biomes;
        this.negate = negate;
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        boolean matches = biomes.contains(surfaceBiome(context, pos));
        return negate != matches;
    }

    private static Holder<Biome> surfaceBiome(PlacementContext context, BlockPos pos) {
        int quartX = pos.getX() >> 2;
        int quartZ = pos.getZ() >> 2;
        int quartY = (context.getLevel().getMaxBuildHeight() - 1) >> 2;
        return context.getLevel().getUncachedNoiseBiome(quartX, quartY, quartZ);
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModPlacementModifiers.SURFACE_BIOME.get();
    }
}
