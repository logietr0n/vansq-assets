package com.vansqmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vansqmod.VansqMod;
import com.vansqmod.entity.IceSnowballProjectile;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Same 3D cube mesh and facing as Amendments snowballs. Paint
 * {@code textures/entity/projectiles/ice_snowball.png} using the snowball_3d unwrap.
 */
public class IceSnowballProjectileRenderer extends EntityRenderer<IceSnowballProjectile> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "textures/entity/projectiles/ice_snowball.png");
    private static final float SCALE = 0.75F;
    private static final int MESH_SIZE = 4;

    private final ModelPart model;

    public IceSnowballProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = createMesh(MESH_SIZE).bakeRoot();
    }

    static LayerDefinition createMesh(int size) {
        int half = size / 2;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "cube",
                CubeListBuilder.create().texOffs(0, 0).addBox(-half, -half, -half, size, size, size),
                PartPose.ZERO);
        root.addOrReplaceChild(
                "cube_emissive",
                CubeListBuilder.create().texOffs(32, 0).addBox(-half, -half, -half, size, size, size),
                PartPose.ZERO);
        root.addOrReplaceChild(
                "overlay",
                CubeListBuilder.create()
                        .texOffs(0, size * 2)
                        .addBox(-(half + 1), -(half + 1), -(half + 1), size + 2, size + 2, size + 2),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ResourceLocation getTextureLocation(IceSnowballProjectile entity) {
        return TEXTURE;
    }

    @Override
    public void render(
            IceSnowballProjectile entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        if (entity.tickCount < 2 && this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25D) {
            return;
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.0F, entity.getBbHeight() / 2.0F, 0.0F);
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.mulPose(Axis.YN.rotationDegrees(180.0F - Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.XN.rotationDegrees(-Mth.rotLerp(partialTicks, entity.xRotO, entity.getXRot())));
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.model.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
