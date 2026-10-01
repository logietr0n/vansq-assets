package com.vansqmod.block;

import com.vansqmod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

public class BuddingRhodoheartBlock extends BuddingAmethystBlock {

    private static final int GROWTH_CHANCE = 5;
    /** Two guaranteed attempts vs one 1/5 attempt is a 10× expected growth rate. */
    private static final int MULTIPLAYER_GROWTH_ATTEMPTS = 2;
    private static final int MULTIPLAYER_PLAYER_THRESHOLD = 2;
    private static final double MULTIPLAYER_RANGE = 32.0D;

    public BuddingRhodoheartBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean boosted = nearbyPlayerCount(level, pos) >= MULTIPLAYER_PLAYER_THRESHOLD;
        int attempts = boosted ? MULTIPLAYER_GROWTH_ATTEMPTS : 1;
        for (int i = 0; i < attempts; i++) {
            if (!boosted && random.nextInt(GROWTH_CHANCE) != 0) {
                continue;
            }
            tryGrowOnce(level, pos, random);
        }
    }

    private static void tryGrowOnce(ServerLevel level, BlockPos pos, RandomSource random) {
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

    private static int nearbyPlayerCount(ServerLevel level, BlockPos pos) {
        Vec3 center = pos.getCenter();
        double rangeSq = MULTIPLAYER_RANGE * MULTIPLAYER_RANGE;
        int count = 0;
        for (ServerPlayer player : level.players()) {
            if (!player.isAlive() || player.isSpectator()) {
                continue;
            }
            if (player.distanceToSqr(center) <= rangeSq) {
                count++;
                if (count >= MULTIPLAYER_PLAYER_THRESHOLD) {
                    return count;
                }
            }
        }
        return count;
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
