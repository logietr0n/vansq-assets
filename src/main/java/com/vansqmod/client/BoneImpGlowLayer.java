package com.vansqmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vansqmod.entity.SkeletonBabies;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class BoneImpGlowLayer<T extends LivingEntity & GeoAnimatable> extends GeoRenderLayer<T> {

    private static final ResourceLocation GLOW =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "textures/entities/bone_imp_e.png");

    public BoneImpGlowLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            T entity,
            BakedGeoModel bakedModel,
            @Nullable RenderType renderType,
            MultiBufferSource bufferSource,
            @Nullable VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        if (!SkeletonBabies.usesBoneImpGeo(entity)) {
            return;
        }
        RenderType eyes = RenderType.eyes(GLOW);
        getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                entity,
                eyes,
                bufferSource.getBuffer(eyes),
                partialTick,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0xFFFFFFFF
        );
    }
}
