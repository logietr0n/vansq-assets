package com.vansqmod.mixin.vanillabackport;

import com.blackgear.vanillabackport.common.api.FallingLeavesModule;
import com.vansqmod.compat.CustomFallingLeaves;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Spawns vansqmod leaf particles (dedicated textures) instead of VB's shared tinted atlas.
 */
@Mixin(FallingLeavesModule.class)
public class FallingLeavesModuleMixin {

    @Inject(method = "spawnFallingLeavesParticle", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$customLeafTextures(Level level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        BlockState state = level.getBlockState(pos);
        SimpleParticleType particle = CustomFallingLeaves.getParticle(state);
        if (particle != null) {
            ParticleUtils.spawnParticleBelow(level, pos, random, particle);
            ci.cancel();
        }
    }
}
