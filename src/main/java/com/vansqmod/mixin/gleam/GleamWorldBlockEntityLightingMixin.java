package com.vansqmod.mixin.gleam;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vansqmod.compat.GleamEntityShaders;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class GleamWorldBlockEntityLightingMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void vansqmod$worldBlockEntityGleam(
            BlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            CallbackInfo ci
    ) {
        GleamEntityShaders.setWorldEntityLighting();
    }
}
