package com.vansqmod.mixin.gleam;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vansqmod.compat.GleamEntityShaders;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Inventory/HUD lighting can leave the Gleam GUI skip set. Clear it on the
 * world-entity path only — paper dolls go through {@code EntityRenderDispatcher}
 * directly and must keep the skip.
 */
@Mixin(LevelRenderer.class)
public abstract class GleamWorldEntityLightingMixin {

    @Inject(method = "renderEntity", at = @At("HEAD"))
    private void vansqmod$worldEntityGleam(
            Entity entity,
            double camX,
            double camY,
            double camZ,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            CallbackInfo ci
    ) {
        GleamEntityShaders.setWorldEntityLighting();
    }
}
