package com.vansqmod.compat;

import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import toni.sodiumleafculling.LeafCullingQuality;
import toni.sodiumleafculling.PerformanceSettingsAccessor;

/**
 * Sodium 0.8 port of Sodium Leaf Culling's mesh checks. The upstream 1.0.1 jar still
 * calls {@code SodiumGameOptions}, which 0.8 renamed to {@code SodiumOptions}.
 */
public final class SodiumLeafCulling {

    private static final Direction[] VALUES = Direction.values();

    private SodiumLeafCulling() {
    }

    public static LeafCullingQuality quality() {
        try {
            Object performance = SodiumClientMod.options().performance;
            if (performance instanceof PerformanceSettingsAccessor accessor) {
                LeafCullingQuality quality = accessor.sodiumleafculling$getQuality();
                if (quality != null) {
                    return quality;
                }
            }
        } catch (Throwable ignored) {
        }
        return LeafCullingQuality.SOLID_AGGRESSIVE;
    }

    public static void setQuality(LeafCullingQuality quality) {
        if (quality == null) {
            return;
        }
        try {
            Object performance = SodiumClientMod.options().performance;
            if (performance instanceof PerformanceSettingsAccessor accessor) {
                accessor.sodiumleafculling$setQuality(quality);
            }
        } catch (Throwable ignored) {
        }
    }

    public static boolean isFacingAir(BlockGetter view, BlockPos pos, Direction facing) {
        return view.getBlockState(pos.offset(facing.getNormal())).getBlock() instanceof AirBlock;
    }

    public static boolean surroundedByLeaves(BlockGetter view, BlockPos pos) {
        boolean aggressive = quality() == LeafCullingQuality.SOLID_AGGRESSIVE;
        for (Direction dir : VALUES) {
            if (aggressive && (dir == Direction.DOWN || dir == Direction.UP)) {
                continue;
            }
            BlockState neighbor = view.getBlockState(pos.offset(dir.getNormal()));
            if (neighbor.getBlock() instanceof LeavesBlock || neighbor.isSolidRender(view, pos)) {
                continue;
            }
            return false;
        }
        return true;
    }

    public static boolean shouldCullSide(BlockGetter view, BlockPos pos, Direction facing, int depth) {
        if (isFacingAir(view, pos, facing)) {
            return false;
        }
        Vec3i normal = facing.getNormal();
        boolean allLeaves = true;
        for (int i = 1; i <= depth; i++) {
            BlockState state = view.getBlockState(pos.offset(normal.multiply(i)));
            allLeaves = allLeaves && state.getBlock() instanceof LeavesBlock;
        }
        return allLeaves;
    }
}
