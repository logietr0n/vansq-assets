package com.vansqmod.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.MultifaceGrowthConfiguration;

import java.util.List;

/**
 * Vanilla {@code multiface_growth} treats water as a valid search/place block, which
 * puts Azalea Growth in lakes. This copy only walks and places in empty-fluid air.
 */
public class AirMultifaceGrowthFeature extends Feature<MultifaceGrowthConfiguration> {

    private static final ResourceLocation ICE_SHEET_ID =
            ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "ice_sheet");

    public AirMultifaceGrowthFeature(Codec<MultifaceGrowthConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<MultifaceGrowthConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        MultifaceGrowthConfiguration config = context.config();
        if (!canOccupy(level, origin)) {
            return false;
        }

        List<Direction> directions = config.getShuffledDirections(random);
        if (placeGrowthIfPossible(level, origin, origin, level.getBlockState(origin), config, directions)) {
            return true;
        }

        BlockPos.MutableBlockPos cursor = origin.mutable();
        for (Direction direction : directions) {
            cursor.set(origin);
            List<Direction> faces = config.getShuffledDirectionsExcept(random, direction.getOpposite());
            for (int i = 0; i < config.searchRange; i++) {
                cursor.move(direction);
                if (!sameChunk(origin, cursor)) {
                    break;
                }
                BlockState state = level.getBlockState(cursor);
                if (!canOccupy(level, cursor) && !state.is(config.placeBlock)) {
                    break;
                }
                if (placeGrowthIfPossible(level, origin, cursor, state, config, faces)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean placeGrowthIfPossible(
            WorldGenLevel level,
            BlockPos origin,
            BlockPos pos,
            BlockState state,
            MultifaceGrowthConfiguration config,
            List<Direction> directions
    ) {
        if (!sameChunk(origin, pos)) {
            return false;
        }
        if (!canOccupy(level, pos) && !state.is(config.placeBlock)) {
            return false;
        }
        BlockPos.MutableBlockPos neighbor = pos.mutable();
        for (Direction direction : directions) {
            BlockState support = level.getBlockState(neighbor.setWithOffset(pos, direction));
            if (!support.is(config.canBePlacedOn)) {
                continue;
            }
            MultifaceBlock block = config.placeBlock;
            BlockState placed = block.getStateForPlacement(state, level, pos, direction);
            if (placed == null) {
                return false;
            }
            level.setBlock(pos, placed, 3);
            level.getChunk(pos).markPosForPostprocessing(pos);
            // Vanilla spreader walks into neighboring chunks and trips "setBlock in a far chunk"
            // during FEATURES. Colonies already retry; skip extra spread here.
            return true;
        }
        return false;
    }

    private static boolean sameChunk(BlockPos a, BlockPos b) {
        return (a.getX() >> 4) == (b.getX() >> 4) && (a.getZ() >> 4) == (b.getZ() >> 4);
    }

    private static boolean canOccupy(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        return state.isAir() || state.canBeReplaced() || isIceSheet(state);
    }

    private static boolean isIceSheet(BlockState state) {
        return ICE_SHEET_ID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }
}
