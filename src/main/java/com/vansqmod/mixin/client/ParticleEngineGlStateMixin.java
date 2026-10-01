package com.vansqmod.mixin.client;

import com.vansqmod.client.ParticleRenderGlState;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

/**
 * Capture the particle-pass framebuffer, then restore depth, lightmap, and that
 * target before every render type. Pickup (CUSTOM) leaks all three.
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineGlStateMixin {

    @Inject(
            method = "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",
            at = @At("HEAD")
    )
    private void vansqmod$captureParticlePass(
            LightTexture lightTexture,
            Camera camera,
            float partialTick,
            Frustum frustum,
            Predicate<ParticleRenderType> renderTypePredicate,
            CallbackInfo ci
    ) {
        ParticleRenderGlState.capturePass(lightTexture);
    }

    @Inject(
            method = "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/ParticleRenderType;begin(Lcom/mojang/blaze3d/vertex/Tesselator;Lnet/minecraft/client/renderer/texture/TextureManager;)Lcom/mojang/blaze3d/vertex/BufferBuilder;"
            )
    )
    private void vansqmod$restoreBeforeType(CallbackInfo ci) {
        ParticleRenderGlState.restoreForParticles();
    }

    @Inject(
            method = "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",
            at = @At("RETURN")
    )
    private void vansqmod$endParticlePass(CallbackInfo ci) {
        ParticleRenderGlState.endPass();
    }
}
