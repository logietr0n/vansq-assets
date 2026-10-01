package com.vansqmod.compat;

import com.vansqmod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Places {@link com.vansqmod.block.ForcedSoulFireBlock} and flags vanilla
 * {@code BaseFireBlock.getState} so other mods cannot swap it to regular fire.
 */
public final class SoulFirePlacer {

    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private SoulFirePlacer() {
    }

    public static boolean isActive() {
        return DEPTH.get() > 0;
    }

    public static void run(Runnable action) {
        DEPTH.set(DEPTH.get() + 1);
        try {
            action.run();
        } finally {
            DEPTH.set(DEPTH.get() - 1);
        }
    }

    public static boolean place(Level level, BlockPos pos) {
        BlockState fire = ModBlocks.SOUL_FIRE.get().defaultBlockState();
        if (!level.getBlockState(pos).isAir() || !fire.canSurvive(level, pos)) {
            return false;
        }
        run(() -> level.setBlock(pos, fire, 3));
        return true;
    }
}
