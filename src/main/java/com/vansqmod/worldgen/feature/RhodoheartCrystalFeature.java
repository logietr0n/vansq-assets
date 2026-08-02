package com.vansqmod.worldgen.feature;

import com.mojang.serialization.Codec;
import com.vansqmod.registry.ModBlocks;
import com.vansqmod.worldgen.feature.config.RhodoheartCrystalFeatureConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.Fluids;

public class RhodoheartCrystalFeature extends Feature<RhodoheartCrystalFeatureConfig> {

    public RhodoheartCrystalFeature(Codec<RhodoheartCrystalFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<RhodoheartCrystalFeatureConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        RhodoheartCrystalFeatureConfig config = context.config();

        if (!level.getBlockState(origin).is(BlockTags.BASE_STONE_OVERWORLD)) {
            return false;
        }

        level.setBlock(origin, ModBlocks.BUDDING_RHODOHEART.get().defaultBlockState(), 2);

        boolean placedCrystal = false;
        for (Direction direction : Direction.values()) {
            if (random.nextFloat() > config.crystal_chance()) {
                continue;
            }

            BlockPos adjacent = origin.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacent);
            if (!adjacentState.isAir() && adjacentState.getFluidState().getType() != Fluids.WATER) {
                continue;
            }

            BlockState crystal = pickCrystal(random, config);
            level.setBlock(
                    adjacent,
                    crystal.setValue(AmethystClusterBlock.FACING, direction)
                            .setValue(AmethystClusterBlock.WATERLOGGED, adjacentState.getFluidState().isSourceOfType(Fluids.WATER)),
                    2);
            placedCrystal = true;
        }

        return placedCrystal || random.nextBoolean();
    }

    private static BlockState pickCrystal(RandomSource random, RhodoheartCrystalFeatureConfig config) {
        float roll = random.nextFloat();
        if (roll < config.glimmering_cluster_chance()) {
            return ModBlocks.GLINTED_RHODOHEART_CLUSTER.get().defaultBlockState();
        }
        roll -= config.glimmering_cluster_chance();
        if (roll < config.large_bud_chance()) {
            return ModBlocks.LARGE_RHODOHEART_BUD.get().defaultBlockState();
        }
        roll -= config.large_bud_chance();
        if (roll < config.medium_bud_chance()) {
            return ModBlocks.MEDIUM_RHODOHEART_BUD.get().defaultBlockState();
        }
        roll -= config.medium_bud_chance();
        if (roll < config.small_bud_chance()) {
            return ModBlocks.SMALL_RHODOHEART_BUD.get().defaultBlockState();
        }
        return ModBlocks.RHODOHEART_CLUSTER.get().defaultBlockState();
    }
}
