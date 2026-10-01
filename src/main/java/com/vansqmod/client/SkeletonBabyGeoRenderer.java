package com.vansqmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.ModList;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SkeletonBabyGeoRenderer<T extends LivingEntity & GeoAnimatable> extends GeoEntityRenderer<T> {

    public SkeletonBabyGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new SkeletonBabyGeoModel<>());
        this.shadowRadius = 0.3F;
        this.addRenderLayer(new BoneImpGlowLayer<>(this));
        if (ModList.get().isLoaded("enchantwithmob")) {
            this.addRenderLayer(new BabySkeletonEnchantedEyesLayer<>(this));
        }
        if (ModList.get().isLoaded("netherexp")) {
            this.addRenderLayer(new JneBurningGeoLayer<>(this));
        }
    }

    @Override
    public void render(
            T entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        this.shadowRadius = 0.3F;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
