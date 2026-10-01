package com.vansqmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Fullbright cutout overlay for Spider Overhaul variants.
 * {@link RenderType#eyes} is additive and many shader packs light the whole mesh
 * on that pass, so cropped transparent PNGs still made the body glow.
 */
public class SpiderOverhaulEyesLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public SpiderOverhaulEyesLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (entity.isInvisible()) {
            return;
        }
        ResourceLocation eyes = SpiderOverhaulEyes.overlayTexture(this.getTextureLocation(entity));
        if (eyes == null) {
            return;
        }
        RenderType renderType = RenderType.entityCutoutNoCull(eyes);
        this.getParentModel().renderToBuffer(
                poseStack,
                buffer.getBuffer(renderType),
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                -1);
    }
}
