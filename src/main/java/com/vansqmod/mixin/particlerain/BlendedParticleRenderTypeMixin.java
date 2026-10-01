package com.vansqmod.mixin.particlerain;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.vansqmod.client.ParticleRenderGlState;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ParticleRain 4.0 beta.11 blended mist never re-enables depth test or the lightmap
 * after CUSTOM pickup particles. Restore the full particle pass here as well.
 */
@Mixin(targets = "pigcart.particlerain.particle.render.BlendedParticleRenderType$1", remap = false)
public abstract class BlendedParticleRenderTypeMixin {

    @Inject(method = "begin", at = @At("HEAD"), remap = false)
    private void vansqmod$enableMistDepth(
            Tesselator tesselator,
            TextureManager textureManager,
            CallbackInfoReturnable<BufferBuilder> cir
    ) {
        ParticleRenderGlState.restoreForParticles();
    }
}
