package com.vansqmod.mixin.enchantwithmob;

import baguchi.enchantwithmob.client.render.layer.EnchantedEyesLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vansqmod.client.VariantEnchantedEyes;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantedEyesLayer.class)
public abstract class EnchantedEyesLayerVariantMixin<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    @Unique
    private T vansqmod$eyeEntity;

    private EnchantedEyesLayerVariantMixin(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD")
    )
    private void vansqmod$captureEyeEntity(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        this.vansqmod$eyeEntity = entity;
    }

    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("RETURN")
    )
    private void vansqmod$clearEyeEntity(CallbackInfo ci) {
        this.vansqmod$eyeEntity = null;
    }

    @ModifyArg(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lbaguchi/enchantwithmob/client/render/layer/EnchantedEyesLayer;enchantedEyes(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
            )
    )
    private ResourceLocation vansqmod$variantEyeTexture(ResourceLocation texture) {
        T entity = this.vansqmod$eyeEntity;
        if (entity == null) {
            return texture;
        }
        return VariantEnchantedEyes.overlayFor(entity, this.getTextureLocation(entity), texture);
    }
}
