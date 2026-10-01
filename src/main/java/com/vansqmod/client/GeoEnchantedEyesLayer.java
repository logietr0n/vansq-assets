package com.vansqmod.client;

import baguchi.enchantwithmob.EnchantConfig;
import baguchi.enchantwithmob.api.IEnchantCap;
import baguchi.enchantwithmob.api.MobEnchantEye;
import baguchi.enchantwithmob.client.render.layer.EnchantedEyesLayer;
import baguchi.enchantwithmob.data.resources.registries.MobEnchantEyes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

/**
 * EnchantWithMob eye overlay for GeckoLib mobs. {@code EnchantedEyesLayer} is only
 * added to {@code LivingEntityRenderer}; Geo renderers need this instead.
 */
public class GeoEnchantedEyesLayer<T extends LivingEntity & GeoAnimatable> extends GeoRenderLayer<T> {

    public GeoEnchantedEyesLayer(GeoRenderer<T> renderer) {
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
        if (!(entity instanceof IEnchantCap cap) || !cap.getEnchantCap().hasEnchant()) {
            return;
        }
        if (EnchantConfig.CLIENT.disableEyeRender.get()) {
            return;
        }
        ResourceLocation texture = resolveTexture(entity);
        if (texture == null) {
            return;
        }
        RenderType eyes = EnchantedEyesLayer.enchantedEyes(texture);
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

    @Nullable
    protected ResourceLocation resolveTexture(T entity) {
        return MobEnchantEyes.getEyeVariant(entity.registryAccess(), entity.getType())
                .map(MobEnchantEye::texture)
                .orElse(null);
    }
}
