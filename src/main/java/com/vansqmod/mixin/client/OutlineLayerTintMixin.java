package com.vansqmod.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vansqmod.compat.HealLightTint;
import com.vansqmod.compat.JneBurning;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Overlay/outline layers draw through {@code renderColoredCutoutModel}. HealLight
 * and JNE's {@code BurningFilterLayer} only touch the parent body pass.
 */
@Mixin(RenderLayer.class)
public abstract class OutlineLayerTintMixin {

    @ModifyVariable(method = "renderColoredCutoutModel", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static int vansqmod$healLight(
            int packedLight,
            EntityModel<?> model,
            ResourceLocation texture,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLightArg,
            LivingEntity entity,
            int color
    ) {
        return HealLightTint.boostLight(entity, packedLight);
    }

    @ModifyVariable(method = "renderColoredCutoutModel", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private static int vansqmod$healColor(
            int color,
            EntityModel<?> model,
            ResourceLocation texture,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            LivingEntity entity,
            int colorArg
    ) {
        return HealLightTint.color(entity, color);
    }

    @Inject(method = "renderColoredCutoutModel", at = @At("RETURN"))
    private static void vansqmod$burn(
            EntityModel<?> model,
            ResourceLocation texture,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            LivingEntity entity,
            int color,
            CallbackInfo ci
    ) {
        JneBurning.renderModel(model, texture, poseStack, buffer, packedLight, entity, 0.0F);
    }
}
