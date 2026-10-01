package com.vansqmod.mixin.separatedleaves;

import com.vansqmod.compat.SeparatedLeavesPerf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin cannot target another mixin class (that crashed launch). This mixin applies
 * to {@link LeavesBlock} after Separated Leaves so {@code VansqMixinConfigPlugin}
 * can patch their merged {@code updateDistance} handler: unlisted leaves skip the
 * per-tick structure scan and biome lookup.
 * <p>
 * The skip helper is merged onto {@code LeavesBlock} so the plugin can invoke it
 * from Minecraft bytecode without a JPMS read of {@code com.vansqmod.compat}.
 */
@Mixin(value = LeavesBlock.class, priority = 1)
public abstract class SeparatedLeavesUpdateDistanceMixin {

    @Unique
    private static boolean vansqmod$shouldSkipSeparatedLeavesScan(BlockState state) {
        try {
            return SeparatedLeavesPerf.shouldSkipUpdateDistance(state);
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Inject(method = "updateDistance", at = @At("HEAD"))
    private static void vansqmod$separatedLeavesPatchAnchor(
            BlockState state,
            LevelAccessor level,
            BlockPos pos,
            CallbackInfoReturnable<BlockState> cir
    ) {
        // Keep a live invoke so Mixin merges the helper onto LeavesBlock (and records
        // the vansqmod dependency). The skip of Separated Leaves' scan is applied in
        // VansqMixinConfigPlugin against their handler.
        vansqmod$shouldSkipSeparatedLeavesScan(state);
    }
}
