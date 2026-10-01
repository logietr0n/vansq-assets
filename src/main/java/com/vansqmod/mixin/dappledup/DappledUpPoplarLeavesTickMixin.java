package com.vansqmod.mixin.dappledup;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dappled Up's poplar leaves {@code tick} calls vanilla distance logic, then a "client display"
 * particle procedure on the server, then {@code scheduleTick(..., 1)} — every leaf, every tick.
 * That queue was ~74% of the integrated-server thread. Keep vanilla decay only; particles go
 * through Vanilla Backport's client {@code animateTick} path.
 */
@Mixin(targets = {
        "net.mornity.dappledup.block.OrangePoplarLeavesBlock",
        "net.mornity.dappledup.block.RedPoplarLeavesBlock",
        "net.mornity.dappledup.block.YellowPoplarLeavesBlock"
})
public abstract class DappledUpPoplarLeavesTickMixin extends LeavesBlock {

    private DappledUpPoplarLeavesTickMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void vansqmod$vanillaLeafTick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random,
            CallbackInfo ci
    ) {
        super.tick(state, level, pos, random);
        ci.cancel();
    }

    @Inject(method = "onPlace", at = @At("HEAD"), cancellable = true)
    private void vansqmod$vanillaLeafOnPlace(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState oldState,
            boolean movedByPiston,
            CallbackInfo ci
    ) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        ci.cancel();
    }
}
