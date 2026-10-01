package com.vansqmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vansqmod.entity.Mellowed;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.neoforged.fml.ModList;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class MellowedRenderer extends GeoEntityRenderer<Mellowed> {

    /** GeckoLib's drowned shiver uses {@code tickCount * 3.25}; walk is twice the previous slowed rate. */
    private static final double WALK_SHAKE_SPEED = 3.25D / 2.0D;

    public MellowedRenderer(EntityRendererProvider.Context context) {
        super(context, new MellowedModel());
        this.shadowRadius = 0.5F;
        this.addRenderLayer(new AdultGlowLayer(this));
        if (ModList.get().isLoaded("enchantwithmob")) {
            this.addRenderLayer(new BabySkeletonEnchantedEyesLayer<>(this));
        }
    }

    @Override
    public void render(
            Mellowed entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        this.shadowRadius = entity.isBaby() ? 0.3F : 0.5F;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    protected void applyRotations(
            Mellowed animatable,
            PoseStack poseStack,
            float ageInTicks,
            float rotationYaw,
            float partialTick,
            float scale
    ) {
        float hurt = hurtShakeIntensity(animatable, partialTick);
        if (hurt > 0.0F) {
            rotationYaw += Mth.sin(ageInTicks * 9.0F) * 8.0F * hurt;
        } else if (animatable.walkAnimation.isMoving() && !super.isShaking(animatable)) {
            rotationYaw += (float) (Math.cos(ageInTicks * WALK_SHAKE_SPEED) * Math.PI * 0.4D);
        }
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, scale);
        if (hurt > 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.cos(ageInTicks * 11.0F) * 5.0F * hurt));
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(ageInTicks * 13.0F) * 3.0F * hurt));
        }
    }

    /** 1 at the hit, quadratic ease back to 0 over {@code hurtDuration}. */
    private static float hurtShakeIntensity(Mellowed mellowed, float partialTick) {
        if (mellowed.hurtTime <= 0 || mellowed.hurtDuration <= 0) {
            return 0.0F;
        }
        float remaining = Mth.clamp((mellowed.hurtTime - partialTick) / mellowed.hurtDuration, 0.0F, 1.0F);
        return remaining * remaining;
    }

    private static final class AdultGlowLayer extends AutoGlowingGeoLayer<Mellowed> {

        private AdultGlowLayer(MellowedRenderer renderer) {
            super(renderer);
        }

        @Override
        public void render(
                PoseStack poseStack,
                Mellowed entity,
                BakedGeoModel bakedModel,
                RenderType renderType,
                MultiBufferSource bufferSource,
                VertexConsumer buffer,
                float partialTick,
                int packedLight,
                int packedOverlay
        ) {
            if (entity.isBaby()) {
                return;
            }
            super.render(
                    poseStack,
                    entity,
                    bakedModel,
                    renderType,
                    bufferSource,
                    buffer,
                    partialTick,
                    packedLight,
                    packedOverlay);
        }
    }
}
