package com.vansqmod.block;

import com.vansqmod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class BuddingRhodoheartBlock extends BuddingAmethystBlock {

    public BuddingRhodoheartBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 0) {
            return;
        }

        Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
        BlockPos adjacent = pos.relative(direction);
        BlockState adjacentState = level.getBlockState(adjacent);

        if (!canGrowInto(adjacentState)) {
            return;
        }

        BlockState next = nextGrowthStage(adjacentState);
        Direction facing;
        if (next == null) {
            next = ModBlocks.SMALL_RHODOHEART_BUD.get().defaultBlockState();
            facing = direction;
        } else {
            facing = adjacentState.getValue(AmethystClusterBlock.FACING);
        }

        level.setBlock(
                adjacent,
                next.setValue(AmethystClusterBlock.FACING, facing)
                        .setValue(AmethystClusterBlock.WATERLOGGED, adjacentState.getFluidState().isSourceOfType(Fluids.WATER)),
                Block.UPDATE_CLIENTS);
    }

    private static BlockState nextGrowthStage(BlockState state) {
        Block block = state.getBlock();
        if (block == ModBlocks.SMALL_RHODOHEART_BUD.get()) {
            return ModBlocks.MEDIUM_RHODOHEART_BUD.get().defaultBlockState();
        }
        if (block == ModBlocks.MEDIUM_RHODOHEART_BUD.get()) {
            return ModBlocks.LARGE_RHODOHEART_BUD.get().defaultBlockState();
        }
        if (block == ModBlocks.LARGE_RHODOHEART_BUD.get()) {
            return ModBlocks.RHODOHEART_CLUSTER.get().defaultBlockState();
        }
        return null;
    }

    private static boolean canGrowInto(BlockState state) {
        if (state.isAir() || state.is(Blocks.WATER)) {
            return true;
        }
        Block block = state.getBlock();
        return block == ModBlocks.SMALL_RHODOHEART_BUD.get()
                || block == ModBlocks.MEDIUM_RHODOHEART_BUD.get()
                || block == ModBlocks.LARGE_RHODOHEART_BUD.get();
    }
}
