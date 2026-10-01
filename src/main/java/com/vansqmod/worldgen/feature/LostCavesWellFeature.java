package com.vansqmod.worldgen.feature;

import com.mojang.serialization.Codec;
import com.vansqmod.worldgen.ArchaeologyLootSeeds;
import com.vansqmod.worldgen.feature.config.LostCavesWellFeatureConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * At most one Lost Caves well per 3D spacing cell. Horizontal cells are chunk columns;
 * vertical cells are 16-block sections so wells stay apart in X, Z, and Y.
 */
public class LostCavesWellFeature extends Feature<LostCavesWellFeatureConfig> {

    private static final ResourceKey<ConfiguredFeature<?, ?>> WELL_TEMPLATE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath("vansq", "lost_caves_well"));
    private static final ResourceKey<LootTable> WELL_ARCHAEOLOGY = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("vansq", "archaeology/lost_caves_well"));
    private static final ResourceKey<net.minecraft.world.level.biome.Biome> LOST_CAVES = ResourceKey.create(
            Registries.BIOME, ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "lost_caves"));

    private static final ResourceLocation ANCIENT_SAND =
            ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "ancient_sand");
    private static final ResourceLocation ANCIENT_SANDSTONE =
            ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "ancient_sandstone");
    private static final ResourceLocation LAYERED_ANCIENT_SANDSTONE =
            ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "layered_ancient_sandstone");
    private static final ResourceLocation SMOOTH_ANCIENT_SANDSTONE =
            ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "smooth_ancient_sandstone");
    private static final ResourceLocation BRITTLE_ANCIENT_SANDSTONE =
            ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "brittle_ancient_sandstone");
    private static final ResourceLocation GILDED_ANCIENT_SANDSTONE =
            ResourceLocation.fromNamespaceAndPath("vansqmod", "gilded_ancient_sandstone");
    private static final ResourceLocation PRICKLY_PEACH =
            ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "prickly_peach_cactus");
    private static final TagKey<Block> WELL_REPLACEABLE = TagKey.create(
            Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("vansq", "lost_caves_well_replaceable"));

    private static final long SALT = 71829446L;
    private static final int FLOOR_RADIUS = 1;
    private static final int AIR_HEIGHT = 6;
    private static final int EDGE_MARGIN = 2;
    private static final int ARCHAEOLOGY_RADIUS = 6;
    private static final int ARCHAEOLOGY_MIN_Y = -2;
    private static final int ARCHAEOLOGY_MAX_Y = 10;

    public LostCavesWellFeature(Codec<LostCavesWellFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<LostCavesWellFeatureConfig> context) {
        LostCavesWellFeatureConfig config = context.config();
        int spacing = Math.max(1, config.spacingChunks());
        int separation = Math.min(config.separationChunks(), spacing - 1);
        int spread = Math.max(1, spacing - separation);
        int spacingY = Math.max(1, config.spacingYSections());
        int separationY = Math.min(config.separationYSections(), spacingY - 1);
        int spreadY = Math.max(1, spacingY - separationY);

        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;
        int regionX = Math.floorDiv(chunkX, spacing);
        int regionZ = Math.floorDiv(chunkZ, spacing);

        Optional<Holder.Reference<ConfiguredFeature<?, ?>>> well = level.registryAccess()
                .lookupOrThrow(Registries.CONFIGURED_FEATURE)
                .get(WELL_TEMPLATE);
        if (well.isEmpty()) {
            return false;
        }
        ChunkGenerator generator = context.chunkGenerator();

        int minSection = Math.floorDiv(config.minY(), 16);
        int maxSection = Math.floorDiv(config.maxY(), 16);
        boolean placed = false;
        int lastRegionY = Integer.MIN_VALUE;
        for (int sectionY = minSection; sectionY <= maxSection; sectionY++) {
            int regionY = Math.floorDiv(sectionY, spacingY);
            if (regionY == lastRegionY) {
                continue;
            }
            lastRegionY = regionY;

            RandomSource cellRandom = RandomSource.create(
                    level.getSeed() + SALT
                            + regionX * 341873128712L
                            + regionY * 132897987541L
                            + regionZ * 42317861L);
            int targetChunkX = regionX * spacing + cellRandom.nextInt(spread);
            int targetChunkZ = regionZ * spacing + cellRandom.nextInt(spread);
            int targetSectionY = regionY * spacingY + cellRandom.nextInt(spreadY);
            if (chunkX != targetChunkX || chunkZ != targetChunkZ) {
                continue;
            }

            int bandMinY = Math.max(config.minY(), targetSectionY * 16);
            int bandMaxY = Math.min(config.maxY(), (targetSectionY + 1) * 16 - 1);
            if (bandMinY > bandMaxY) {
                continue;
            }

            List<BlockPos> sites = findSites(level, chunkX, chunkZ, bandMinY, bandMaxY);
            if (sites.isEmpty()) {
                continue;
            }
            BlockPos site = sites.get(cellRandom.nextInt(sites.size()));
            RandomSource placeRandom = RandomSource.create(
                    cellRandom.nextLong() ^ BlockPos.asLong(site.getX(), site.getY(), site.getZ()));
            if (well.get().value().place(level, generator, placeRandom, site)) {
                reseedArchaeologyLoot(level, site);
                placed = true;
            }
        }
        return placed;
    }

    /**
     * The well template bakes two loot seeds onto YUNG suspicious ancient sand.
     * Vanilla does not randomize those, and they are not {@code BrushableBlockEntity}.
     */
    private static void reseedArchaeologyLoot(WorldGenLevel level, BlockPos origin) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -ARCHAEOLOGY_RADIUS; dx <= ARCHAEOLOGY_RADIUS; dx++) {
            for (int dz = -ARCHAEOLOGY_RADIUS; dz <= ARCHAEOLOGY_RADIUS; dz++) {
                for (int dy = ARCHAEOLOGY_MIN_Y; dy <= ARCHAEOLOGY_MAX_Y; dy++) {
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    BlockEntity blockEntity = level.getBlockEntity(cursor);
                    if (blockEntity != null) {
                        ArchaeologyLootSeeds.apply(blockEntity, WELL_ARCHAEOLOGY);
                    }
                }
            }
        }
    }

    private static List<BlockPos> findSites(WorldGenLevel level, int chunkX, int chunkZ, int minY, int maxY) {
        List<BlockPos> sites = new ArrayList<>();
        int minX = (chunkX << 4) + EDGE_MARGIN;
        int minZ = (chunkZ << 4) + EDGE_MARGIN;
        int maxX = (chunkX << 4) + 15 - EDGE_MARGIN;
        int maxZ = (chunkZ << 4) + 15 - EDGE_MARGIN;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = maxY; y >= minY; y--) {
                    cursor.set(x, y, z);
                    if (!level.getBiome(cursor).is(LOST_CAVES)) {
                        continue;
                    }
                    if (isOpenChamberFloor(level, cursor)) {
                        sites.add(cursor.immutable());
                    }
                }
            }
        }
        return sites;
    }

    private static boolean isOpenChamberFloor(WorldGenLevel level, BlockPos origin) {
        if (!isWellFloor(level.getBlockState(origin))) {
            return false;
        }
        if (!isSupport(level.getBlockState(origin.below())) || !isSupport(level.getBlockState(origin.below(2)))) {
            return false;
        }
        for (int dx = -FLOOR_RADIUS; dx <= FLOOR_RADIUS; dx++) {
            for (int dz = -FLOOR_RADIUS; dz <= FLOOR_RADIUS; dz++) {
                if (!isWellFloor(level.getBlockState(origin.offset(dx, 0, dz)))) {
                    return false;
                }
                if (!isOpen(level.getBlockState(origin.offset(dx, 1, dz)))) {
                    return false;
                }
            }
        }
        for (int dy = 1; dy <= AIR_HEIGHT; dy++) {
            if (!isOpen(level.getBlockState(origin.above(dy)))) {
                return false;
            }
        }
        return !hasNearbyFluid(level, origin);
    }

    /**
     * The well is about 5×5. Reject sites whose footprint or air column already
     * contains water or lava so they do not generate inside lakes or lava pools.
     */
    private static boolean hasNearbyFluid(WorldGenLevel level, BlockPos origin) {
        int radius = 2;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 1; dy <= AIR_HEIGHT; dy++) {
                    if (!level.getFluidState(origin.offset(dx, dy, dz)).isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isWellFloor(BlockState state) {
        return isBlock(state, ANCIENT_SAND)
                || isBlock(state, ANCIENT_SANDSTONE)
                || isBlock(state, LAYERED_ANCIENT_SANDSTONE)
                || isBlock(state, GILDED_ANCIENT_SANDSTONE);
    }

    private static boolean isSupport(BlockState state) {
        return isWellFloor(state)
                || isBlock(state, SMOOTH_ANCIENT_SANDSTONE)
                || isBlock(state, BRITTLE_ANCIENT_SANDSTONE);
    }

    private static boolean isOpen(BlockState state) {
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        return state.isAir()
                || state.canBeReplaced()
                || state.is(BlockTags.REPLACEABLE)
                || state.is(WELL_REPLACEABLE)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.DEAD_BUSH)
                || isBlock(state, PRICKLY_PEACH);
    }

    private static boolean isBlock(BlockState state, ResourceLocation id) {
        Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
        return block != null && state.is(block);
    }
}
