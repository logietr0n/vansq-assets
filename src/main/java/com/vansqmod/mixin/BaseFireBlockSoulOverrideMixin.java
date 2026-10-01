package com.vansqmod.mixin;

import com.vansqmod.compat.SoulFirePlacer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseFireBlock.class)
public abstract class BaseFireBlockSoulOverrideMixin {

    @Inject(method = "getState", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$forcedSoulFire(
            BlockGetter level,
            BlockPos pos,
            CallbackInfoReturnable<BlockState> cir
    ) {
        if (SoulFirePlacer.isActive()) {
            cir.setReturnValue(com.vansqmod.registry.ModBlocks.SOUL_FIRE.get().defaultBlockState());
        }
    }
}
