package com.vansqmod.mixin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vansqmod.client.ParticleRenderGlState;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ItemPickupParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Item/XP pickup renders an entity and {@code endBatch()} leaves depth testing off,
 * the lightmap unbound, and (on Fabulous) the wrong framebuffer. Restore the particle
 * pass so mist keeps drawing for the rest of this type and the next.
 */
@Mixin(ItemPickupParticle.class)
public abstract class ItemPickupParticleGlStateMixin {

    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V",
            at = @At("RETURN")
    )
    private void vansqmod$restoreDepthAfterPickup(VertexConsumer buffer, Camera camera, float partialTick, CallbackInfo ci) {
        ParticleRenderGlState.restoreForParticles();
    }
}
