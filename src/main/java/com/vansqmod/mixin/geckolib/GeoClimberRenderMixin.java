package com.vansqmod.mixin.geckolib;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nyfaria.nyfsspiders.client.ClientEventHandlers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Nyf rotates climbers inside {@code LivingEntityRenderer}. Born in Chaos spiders are drawn by
 * GeckoLib, which skips that hook, so the body never lies against walls or ceilings.
 */
@Mixin(value = GeoEntityRenderer.class, remap = false)
public class GeoClimberRenderMixin {

    @Inject(
            method = "render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD")
    )
    private void vansqmod$climberPre(
            Entity entity,
            float yaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        if (entity instanceof LivingEntity living) {
            ClientEventHandlers.onPreRenderLiving(living, partialTick, poseStack);
        }
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN")
    )
    private void vansqmod$climberPost(
            Entity entity,
            float yaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        if (entity instanceof LivingEntity living) {
            ClientEventHandlers.onPostRenderLiving(living, partialTick, poseStack, buffer);
        }
    }
}
