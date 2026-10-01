package com.vansqmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

/**
 * Amendments-style tumbling cube using a dedicated projectile texture the player can edit.
 */
public class TumblingCubeProjectileRenderer<T extends Entity> extends EntityRenderer<T> {

    private final ResourceLocation texture;
    private final ResourceLocation overlay;
    private final boolean fullBright;

    public TumblingCubeProjectileRenderer(
            EntityRendererProvider.Context context,
            ResourceLocation texture,
            boolean fullBright
    ) {
        this(context, texture, null, fullBright);
    }

    public TumblingCubeProjectileRenderer(
            EntityRendererProvider.Context context,
            ResourceLocation texture,
            ResourceLocation overlay,
            boolean fullBright
    ) {
        super(context);
        this.texture = texture;
        this.overlay = overlay;
        this.fullBright = fullBright;
        this.shadowRadius = 0.15F;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return this.texture;
    }

    @Override
    protected int getBlockLightLevel(T entity, net.minecraft.core.BlockPos pos) {
        return this.fullBright ? 15 : super.getBlockLightLevel(entity, pos);
    }

    @Override
    public void render(
            T entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        poseStack.pushPose();
        poseStack.translate(0.0F, entity.getBbHeight() * 0.5F, 0.0F);
        float age = entity.tickCount + partialTicks;
        poseStack.mulPose(Axis.YP.rotationDegrees(age * 21.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(age * 13.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(age * 7.0F));
        float scale = Math.max(entity.getBbWidth(), 0.25F);
        poseStack.scale(scale, scale, scale);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.texture));
        PoseStack.Pose pose = poseStack.last();
        int light = this.fullBright ? 0xF000F0 : packedLight;
        cube(consumer, pose, light);
        if (this.overlay != null) {
            VertexConsumer overlayConsumer = buffer.getBuffer(RenderType.entityTranslucent(this.overlay));
            cube(overlayConsumer, pose, light);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void cube(VertexConsumer consumer, PoseStack.Pose pose, int light) {
        face(consumer, pose, light, new Vector3f(0, 0, 1),
                -0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F);
        face(consumer, pose, light, new Vector3f(0, 0, -1),
                0.5F, -0.5F, -0.5F, -0.5F, -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F);
        face(consumer, pose, light, new Vector3f(0, 1, 0),
                -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, -0.5F, -0.5F, 0.5F, -0.5F);
        face(consumer, pose, light, new Vector3f(0, -1, 0),
                -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, -0.5F, -0.5F, 0.5F);
        face(consumer, pose, light, new Vector3f(-1, 0, 0),
                -0.5F, -0.5F, -0.5F, -0.5F, -0.5F, 0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, -0.5F);
        face(consumer, pose, light, new Vector3f(1, 0, 0),
                0.5F, -0.5F, 0.5F, 0.5F, -0.5F, -0.5F, 0.5F, 0.5F, -0.5F, 0.5F, 0.5F, 0.5F);
    }

    private static void face(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            int light,
            Vector3f normal,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4
    ) {
        vertex(consumer, pose, light, x1, y1, z1, 0.0F, 1.0F, normal);
        vertex(consumer, pose, light, x2, y2, z2, 1.0F, 1.0F, normal);
        vertex(consumer, pose, light, x3, y3, z3, 1.0F, 0.0F, normal);
        vertex(consumer, pose, light, x4, y4, z4, 0.0F, 0.0F, normal);
    }

    private static void vertex(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            int light,
            float x,
            float y,
            float z,
            float u,
            float v,
            Vector3f normal
    ) {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, normal.x, normal.y, normal.z);
    }
}
