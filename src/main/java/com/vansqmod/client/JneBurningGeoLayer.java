package com.vansqmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vansqmod.compat.JneBurning;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * JNE's {@code BurningFilterLayer} only attaches to {@code LivingEntityRenderer}.
 * Geo mobs (including outer/outline bones in the same mesh) need this extra pass.
 */
public class JneBurningGeoLayer<T extends Entity & GeoAnimatable> extends GeoRenderLayer<T> {

    public JneBurningGeoLayer(GeoRenderer<T> renderer) {
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
        if (!(entity instanceof LivingEntity living) || !JneBurning.shouldDraw(living)) {
            return;
        }
        ResourceLocation texture = getRenderer().getTextureLocation(entity);
        RenderType fire = JneBurning.fireOverlay(texture);
        if (fire == null) {
            return;
        }
        getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                entity,
                fire,
                bufferSource.getBuffer(fire),
                partialTick,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                JneBurning.glowColor(living, partialTick)
        );
    }
}
