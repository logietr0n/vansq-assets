package com.vansqmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vansqmod.integration.tetherpotion.TetherPotionEquipment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Draws a Caverns &amp; Chasms wearable potion on Curios {@code head} using C&amp;C's armor textures.
 * Scaled 5% so the mesh does not z-fight with a helmet.
 */
public final class TetherPotionCurioRenderer implements ICurioRenderer {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            TetherPotionEquipment.CNC_MODID, "textures/models/armor/tether_potion.png");
    private static final ResourceLocation OVERLAY = ResourceLocation.fromNamespaceAndPath(
            TetherPotionEquipment.CNC_MODID, "textures/models/armor/tether_potion_overlay.png");
    private static final ResourceLocation OVERLAY_SUBTLE = ResourceLocation.fromNamespaceAndPath(
            TetherPotionEquipment.CNC_MODID, "textures/models/armor/tether_potion_overlay_subtle.png");
    private static final float HEAD_SCALE = 1.05F;
    private static final int DEFAULT_POTION_COLOR = 0x385DC6;

    private HumanoidModel<LivingEntity> model;

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

        HumanoidModel<LivingEntity> potionModel = model();
        copyPlayerHeadPose(playerModel, potionModel);
        potionModel.setAllVisible(false);
        potionModel.head.visible = true;
        potionModel.hat.visible = true;
        scaleHead(potionModel, HEAD_SCALE);

        int rgb = potionColor(stack);
        float r = ((rgb >> 16) & 0xFF) / 255.0F;
        float g = ((rgb >> 8) & 0xFF) / 255.0F;
        float b = (rgb & 0xFF) / 255.0F;
        int tint = FastColor.ARGB32.colorFromFloat(1.0F, r, g, b);
        int white = FastColor.ARGB32.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F);

        renderColored(potionModel, poseStack, buffer, light, TEXTURE, tint);
        ResourceLocation overlay = TetherPotionEquipment.isSubtle(stack) ? OVERLAY_SUBTLE : OVERLAY;
        renderColored(potionModel, poseStack, buffer, light, overlay, white);
        if (stack.hasFoil()) {
            potionModel.renderToBuffer(
                    poseStack,
                    buffer.getBuffer(RenderType.armorEntityGlint()),
                    light,
                    OverlayTexture.NO_OVERLAY
            );
        }
    }

    private HumanoidModel<LivingEntity> model() {
        if (model == null) {
            model = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
        }
        return model;
    }

    private static void renderColored(
            HumanoidModel<LivingEntity> potionModel,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            ResourceLocation texture,
            int color
    ) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.armorCutoutNoCull(texture));
        potionModel.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, color);
    }

    private static int potionColor(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        return contents != null ? contents.getColor() : DEFAULT_POTION_COLOR;
    }

    private static void scaleHead(HumanoidModel<LivingEntity> potionModel, float scale) {
        potionModel.head.xScale = scale;
        potionModel.head.yScale = scale;
        potionModel.head.zScale = scale;
        potionModel.hat.xScale = scale;
        potionModel.hat.yScale = scale;
        potionModel.hat.zScale = scale;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void copyPlayerHeadPose(HumanoidModel<?> playerModel, HumanoidModel<LivingEntity> potionModel) {
        playerModel.copyPropertiesTo((HumanoidModel) potionModel);
        potionModel.head.copyFrom(playerModel.head);
        potionModel.hat.copyFrom(playerModel.hat);
    }
}
