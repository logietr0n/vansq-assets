package com.vansqmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Draws the Missionary hat with Born in Chaos's custom head model while it is in Curios {@code head}.
 * Head pose is copied from the player model (same as vanilla helmet armor) so crouch sits correctly.
 */
public final class MissionaryHatCurioRenderer implements ICurioRenderer {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.parse("born_in_chaos_v1:textures/entities/missionaryhat.png");
    /** Slightly larger than a helmet so the meshes do not z-fight. */
    private static final float HEAD_SCALE = 1.005F;

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource buffer,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (!(renderLayerParent.getModel() instanceof HumanoidModel<?> playerModel)) {
            return;
        }
        LivingEntity entity = slotContext.entity();
        @SuppressWarnings("unchecked")
        HumanoidModel<LivingEntity> hatModel =
                (HumanoidModel<LivingEntity>) IClientItemExtensions.of(stack)
                        .getHumanoidArmorModel(entity, stack, EquipmentSlot.HEAD, playerModel);

        // Vanilla helmet armor copies the posed player model. Do not also apply
        // Curios sneak translate/rotate — that double-offsets the already-crouched head.
        copyPlayerHeadPose(playerModel, hatModel);
        hatModel.head.xScale *= HEAD_SCALE;
        hatModel.head.yScale *= HEAD_SCALE;
        hatModel.head.zScale *= HEAD_SCALE;
        hatModel.setAllVisible(false);
        hatModel.head.visible = true;

        VertexConsumer consumer = buffer.getBuffer(RenderType.armorCutoutNoCull(TEXTURE));
        hatModel.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void copyPlayerHeadPose(HumanoidModel<?> playerModel, HumanoidModel<LivingEntity> hatModel) {
        playerModel.copyPropertiesTo((HumanoidModel) hatModel);
        hatModel.head.copyFrom(playerModel.head);
    }
}
