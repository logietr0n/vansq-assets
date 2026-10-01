package com.vansqmod.mixin;

import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla {@link CaveVines#hasGlowBerries} only checks the {@code berries} property.
 * YUNG's Cave Biomes prickly-peach cactus reuses that same property for fruit, so
 * Friends & Foes Glares path to it, take cactus damage, and die.
 */
@Mixin(CaveVines.class)
public interface CaveVinesHasGlowBerriesMixin {

    @Inject(method = "hasGlowBerries", at = @At("RETURN"), cancellable = true)
    private static void vansqmod$requireActualCaveVines(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && !(state.getBlock() instanceof CaveVines)) {
            cir.setReturnValue(false);
        }
    }
}
