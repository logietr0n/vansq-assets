package com.vansqmod.mixin.gleam;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vansqmod.compat.GleamEntityShaders;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * First-person items share entity shaders with world meshes, but their pose is
 * camera-space. Tell Gleam to sample the camera cell so color matches nearby
 * lights instead of an empty grid lookup. Hotbar/GUI still uses the zero matrix.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class GleamItemInHandGleamMixin {

    @Inject(method = "renderHandsWithItems", at = @At("HEAD"))
    private void vansqmod$beginHandGleam(
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            LocalPlayer player,
            int combinedLight,
            CallbackInfo ci
    ) {
        GleamEntityShaders.setFirstPersonHand(true);
    }

    @Inject(method = "renderHandsWithItems", at = @At("RETURN"))
    private void vansqmod$endHandGleam(CallbackInfo ci) {
        GleamEntityShaders.setFirstPersonHand(false);
    }
}
