package com.vansqmod.worldgen;

import com.vansqmod.VansqMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.RandomizableContainer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Biome-specific monster room variants. Lost Caves rooms are taller sandstone dungeons;
 * Frosted Caves rooms are wider permafrost rectangles. Deepslate-layer rooms are larger
 * hanging-spawner dungeons and only apply when no biome variant matched.
 */
public final class BiomeMonsterRooms {

    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    private static final ResourceKey<Biome> LOST_CAVES = ResourceKey.create(
            Registries.BIOME, ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "lost_caves"));
    private static final ResourceKey<Biome> FROSTED_CAVES = ResourceKey.create(
            Registries.BIOME, ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "frosted_caves"));

    private static final ResourceLocation CUT_ANCIENT_SANDSTONE =
            ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "cut_ancient_sandstone");
    private static final ResourceLocation ANCIENT_SANDSTONE =
            ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "ancient_sandstone");
    private static final ResourceLocation PERMAFROST_BRICKS =
            ResourceLocation.fromNamespaceAndPath("quark", "permafrost_bricks");
    private static final ResourceLocation PERMAFROST =
            ResourceLocation.fromNamespaceAndPath("quark", "permafrost");
    private static final ResourceLocation COBBLED_DEEPSLATE_BRICKS =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "cobbled_deepslate_bricks");
    public static final ResourceLocation COBBLESTONE_BRICKS =
            ResourceLocation.fromNamespaceAndPath("quark", "cobblestone_bricks");
    public static final ResourceLocation MOSSY_COBBLESTONE_BRICKS =
            ResourceLocation.fromNamespaceAndPath("quark", "mossy_cobblestone_bricks");

    private static final List<MobWeight> LOST_CAVES_MOBS = List.of(
            new MobWeight("minecraft:husk", 100),
            new MobWeight("alexsmobs:guster", 200)
    );
    private static final List<MobWeight> FROSTED_CAVES_MOBS = List.of(
            new MobWeight("yungscavebiomes:ice_cube", 200),
            new MobWeight("variantsandventures:gelid", 100)
    );
    private static final List<MobWeight> VANILLA_MOBS = List.of(
            new MobWeight("minecraft:skeleton", 100),
            new MobWeight("minecraft:zombie", 200),
            new MobWeight("minecraft:spider", 100)
    );

    private static final ResourceKey<LootTable> LOST_CAVES_CHEST = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("vansq", "chests/lost_caves_monster_room"));
    private static final ResourceKey<LootTable> FROSTED_CAVES_CHEST = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("vansq", "chests/frosted_caves_monster_room"));
    private static final ResourceKey<LootTable> SIMPLE_DUNGEON = ResourceKey.create(
            Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace("chests/simple_dungeon"));

    private BiomeMonsterRooms() {
    }

    /**
     * @return {@code null} if this biome uses the vanilla room; otherwise whether the variant placed
     */
    @Nullable
    public static Boolean placeIfVariant(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        Holder<Biome> biome = context.level().getBiome(context.origin());
        if (biome.is(LOST_CAVES)) {
            return place(context, new Variant(
                    CUT_ANCIENT_SANDSTONE, ANCIENT_SANDSTONE, 4, false, 0, false, LOST_CAVES_MOBS, LOST_CAVES_CHEST));
        }
        if (biome.is(FROSTED_CAVES)) {
            return place(context, new Variant(
                    PERMAFROST_BRICKS, PERMAFROST, 0, true, 0, false, FROSTED_CAVES_MOBS, FROSTED_CAVES_CHEST));
        }
        if (isDeepslateContext(context.level(), context.origin())) {
            return place(context, new Variant(
                    COBBLED_DEEPSLATE_BRICKS,
                    ResourceLocation.withDefaultNamespace("cobbled_deepslate"),
                    2,
                    false,
                    1,
                    true,
                    VANILLA_MOBS,
                    SIMPLE_DUNGEON
            ));
        }
        return null;
    }

    public static Block quarkBrickOr(ResourceLocation id, Block fallback) {
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }

    private static boolean isDeepslateContext(WorldGenLevel level, BlockPos origin) {
        int deepslate = 0;
        int solid = 0;
        BlockPos[] samples = {
                origin.below(),
                origin.above(4),
                origin.offset(2, -1, 0),
                origin.offset(-2, -1, 0),
                origin.offset(0, -1, 2),
                origin.offset(0, -1, -2)
        };
        for (BlockPos pos : samples) {
            BlockState state = level.getBlockState(pos);
            if (!state.isSolid()) {
                continue;
            }
            solid++;
            if (isDeepslateBlock(state)) {
                deepslate++;
            }
        }
        return solid > 0 && deepslate * 2 >= solid;
    }

    private static boolean isDeepslateBlock(BlockState state) {
        return state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.COBBLED_DEEPSLATE)
                || state.is(Blocks.CHISELED_DEEPSLATE)
                || state.is(Blocks.POLISHED_DEEPSLATE)
                || state.is(Blocks.DEEPSLATE_BRICKS)
                || state.is(Blocks.DEEPSLATE_TILES)
                || state.is(Blocks.TUFF);
    }

    private static boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context, Variant variant) {
        Predicate<BlockState> replaceable = Feature.isReplaceable(BlockTags.FEATURES_CANNOT_REPLACE);
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        WorldGenLevel level = context.level();

        int xRadius = random.nextInt(2) + 2 + variant.extraRadius();
        int zRadius = random.nextInt(2) + 2 + variant.extraRadius();
        if (variant.extraWidth()) {
            if (random.nextBoolean()) {
                xRadius += 2;
            } else {
                zRadius += 2;
            }
        }

        int xMin = -xRadius - 1;
        int xMax = xRadius + 1;
        int zMin = -zRadius - 1;
        int zMax = zRadius + 1;
        int maxY = 4 + variant.extraHeight();
        int openings = 0;

        for (int x = xMin; x <= xMax; x++) {
            for (int y = -1; y <= maxY; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    boolean solid = level.getBlockState(pos).isSolid();
                    if (y == -1 && !solid) {
                        return false;
                    }
                    if (y == maxY && !solid) {
                        return false;
                    }
                    if ((x == xMin || x == xMax || z == zMin || z == zMax)
                            && y == 0
                            && level.isEmptyBlock(pos)
                            && level.isEmptyBlock(pos.above())) {
                        openings++;
                    }
                }
            }
        }

        if (openings < 1 || openings > 5) {
            return false;
        }

        BlockState cobble = blockOr(variant.cobbleId(), Blocks.COBBLESTONE);
        BlockState mossy = blockOr(variant.mossyId(), Blocks.MOSSY_COBBLESTONE);

        for (int x = xMin; x <= xMax; x++) {
            for (int y = maxY - 1; y >= -1; y--) {
                for (int z = zMin; z <= zMax; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    boolean wall = x == xMin || y == -1 || z == zMin || x == xMax || y == maxY || z == zMax;
                    if (wall) {
                        if (pos.getY() >= level.getMinBuildHeight() && !level.getBlockState(pos.below()).isSolid()) {
                            level.setBlock(pos, AIR, 2);
                        } else if (state.isSolid() && !state.is(Blocks.CHEST)) {
                            if (y == -1 && random.nextInt(4) != 0) {
                                safeSetBlock(level, pos, mossy, replaceable);
                            } else {
                                safeSetBlock(level, pos, cobble, replaceable);
                            }
                        }
                    } else if (!state.is(Blocks.CHEST) && !state.is(Blocks.SPAWNER)) {
                        safeSetBlock(level, pos, AIR, replaceable);
                    }
                }
            }
        }

        for (int chest = 0; chest < 2; chest++) {
            for (int attempt = 0; attempt < 3; attempt++) {
                int x = origin.getX() + random.nextInt(xRadius * 2 + 1) - xRadius;
                int y = origin.getY();
                int z = origin.getZ() + random.nextInt(zRadius * 2 + 1) - zRadius;
                BlockPos chestPos = new BlockPos(x, y, z);
                if (!level.isEmptyBlock(chestPos)) {
                    continue;
                }
                int solidSides = 0;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    if (level.getBlockState(chestPos.relative(direction)).isSolid()) {
                        solidSides++;
                    }
                }
                if (solidSides == 1) {
                    safeSetBlock(
                            level,
                            chestPos,
                            StructurePiece.reorient(level, chestPos, Blocks.CHEST.defaultBlockState()),
                            replaceable
                    );
                    RandomizableContainer.setBlockEntityLootTable(level, random, chestPos, variant.lootTable());
                    break;
                }
            }
        }

        BlockPos spawnerPos = variant.hangingSpawner() ? origin.above(maxY - 2) : origin;
        if (variant.hangingSpawner()) {
            BlockState chain = Blocks.CHAIN.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Y);
            safeSetBlock(level, origin.above(maxY - 1), chain, replaceable);
        }
        safeSetBlock(level, spawnerPos, Blocks.SPAWNER.defaultBlockState(), replaceable);
        BlockEntity blockEntity = level.getBlockEntity(spawnerPos);
        if (blockEntity instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(randomMob(random, variant.mobs()), random);
        } else {
            VansqMod.LOGGER.error(
                    "Failed to fetch mob spawner entity at ({}, {}, {})",
                    spawnerPos.getX(), spawnerPos.getY(), spawnerPos.getZ()
            );
        }
        return true;
    }

    private static EntityType<?> randomMob(RandomSource random, List<MobWeight> pool) {
        List<ResolvedMob> resolved = new ArrayList<>(pool.size());
        int total = 0;
        for (MobWeight entry : pool) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(entry.id()).orElse(null);
            if (type == null) {
                continue;
            }
            resolved.add(new ResolvedMob(type, entry.weight()));
            total += entry.weight();
        }
        if (resolved.isEmpty() || total <= 0) {
            return EntityType.ZOMBIE;
        }
        int roll = random.nextInt(total);
        for (ResolvedMob mob : resolved) {
            roll -= mob.weight();
            if (roll < 0) {
                return mob.type();
            }
        }
        return resolved.getLast().type();
    }

    private static BlockState blockOr(ResourceLocation id, Block fallback) {
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback).defaultBlockState();
    }

    private static void safeSetBlock(
            WorldGenLevel level,
            BlockPos pos,
            BlockState state,
            Predicate<BlockState> replaceable
    ) {
        if (replaceable.test(level.getBlockState(pos))) {
            level.setBlock(pos, state, 2);
        }
    }

    private record Variant(
            ResourceLocation cobbleId,
            ResourceLocation mossyId,
            int extraHeight,
            boolean extraWidth,
            int extraRadius,
            boolean hangingSpawner,
            List<MobWeight> mobs,
            ResourceKey<LootTable> lootTable
    ) {
    }

    private record MobWeight(ResourceLocation id, int weight) {
        private MobWeight(String id, int weight) {
            this(ResourceLocation.parse(id), weight);
        }
    }

    private record ResolvedMob(EntityType<?> type, int weight) {
    }
}
