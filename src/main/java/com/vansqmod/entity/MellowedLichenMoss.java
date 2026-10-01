package com.vansqmod.entity;

import com.vansqmod.registry.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathfindingContext;

/**
 * Lichen moss / Glow Moss Carpet helpers for Mellowed pathfinding.
 */
public final class MellowedLichenMoss {

    private MellowedLichenMoss() {
    }

    public static boolean isLichenMoss(BlockState state) {
        return state.is(ModBlockTags.MELLOWED_PATHING);
    }

    public static boolean isOnLichenMoss(Entity entity) {
        if (isLichenMoss(entity.getBlockStateOn())) {
            return true;
        }
        BlockPos feet = entity.blockPosition();
        BlockGetter level = entity.level();
        return isLichenMoss(level.getBlockState(feet))
                || isLichenMoss(level.getBlockState(entity.getOnPos()));
    }

    /** True if a path node at feet {@code (x, y, z)} is standing on tagged moss. */
    public static boolean isStandableNode(PathfindingContext context, int x, int y, int z) {
        return isStandableNode(context.level(), x, y, z);
    }

    public static boolean isStandableNode(BlockGetter level, int x, int y, int z) {
        BlockPos feet = new BlockPos(x, y, z);
        BlockState atFeet = level.getBlockState(feet);
        if (isLichenMoss(atFeet)) {
            return true;
        }
        BlockState below = level.getBlockState(feet.below());
        return isLichenMoss(below) && !atFeet.blocksMotion();
    }
}
