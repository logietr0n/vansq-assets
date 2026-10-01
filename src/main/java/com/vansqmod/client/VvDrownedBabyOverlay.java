package com.vansqmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vansqmod.compat.HealLightTint;
import com.vansqmod.compat.JneBurning;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Copies posed baby parts onto the drowned-baby outer mesh. Tiny Takeover's
 * {@code BabyZombieModel#copyPropertiesTo} nudges adult overlay/armor parts,
 * which would misalign a matching baby clothes layer.
 */
public final class VvDrownedBabyOverlay {

    private VvDrownedBabyOverlay() {
    }

    public static boolean isBabyMesh(EntityModel<?> parent) {
        return parent.getClass().getName().contains("Baby");
    }

    public static void render(
            EntityModel<?> parent,
            HumanoidModel<?> overlay,
            ResourceLocation texture,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            LivingEntity entity,
            float partialTick
    ) {
        if (!(parent instanceof HumanoidModel<?> humanoidParent)) {
            return;
        }
        overlay.young = false;
        overlay.attackTime = humanoidParent.attackTime;
        overlay.riding = humanoidParent.riding;
        overlay.crouching = humanoidParent.crouching;
        overlay.leftArmPose = humanoidParent.leftArmPose;
        overlay.rightArmPose = humanoidParent.rightArmPose;
        overlay.head.copyFrom(humanoidParent.head);
        overlay.hat.copyFrom(humanoidParent.hat);
        overlay.body.copyFrom(humanoidParent.body);
        overlay.rightArm.copyFrom(humanoidParent.rightArm);
        overlay.leftArm.copyFrom(humanoidParent.leftArm);
        overlay.rightLeg.copyFrom(humanoidParent.rightLeg);
        overlay.leftLeg.copyFrom(humanoidParent.leftLeg);

        int light = HealLightTint.boostLight(entity, packedLight);
        int color = HealLightTint.color(entity, -1);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        overlay.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, color);
        JneBurning.renderModel(overlay, texture, poseStack, buffer, packedLight, entity, partialTick);
    }
}
