package com.vansqmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vansqmod.VansqMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Draws a crown on Curios {@code head} with the player outer-armor head mesh,
 * scaled 5% so it does not z-fight a helmet. Same approach as the tether potions.
 */
public final class CrownCurioRenderer implements ICurioRenderer {

    public static final ResourceLocation GOLDEN = ResourceLocation.fromNamespaceAndPath(
            VansqMod.MODID, "textures/models/armor/golden_crown.png");
    public static final ResourceLocation SILVER = ResourceLocation.fromNamespaceAndPath(
            VansqMod.MODID, "textures/models/armor/silver_crown.png");

    private static final float HEAD_SCALE = 1.05F;

    private final ResourceLocation texture;
    private HumanoidModel<LivingEntity> model;

    public CrownCurioRenderer(ResourceLocation texture) {
        this.texture = texture;
    }

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

        HumanoidModel<LivingEntity> crown = model();
        copyPlayerHeadPose(playerModel, crown);
        crown.setAllVisible(false);
        crown.head.visible = true;
        crown.hat.visible = true;
        scaleHead(crown, HEAD_SCALE);
        int white = FastColor.ARGB32.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F);
        crown.renderToBuffer(
                poseStack,
                buffer.getBuffer(RenderType.armorCutoutNoCull(texture)),
                light,
                OverlayTexture.NO_OVERLAY,
                white
        );
        if (stack.hasFoil()) {
            crown.renderToBuffer(
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

    private static void scaleHead(HumanoidModel<LivingEntity> crown, float scale) {
        crown.head.xScale = scale;
        crown.head.yScale = scale;
        crown.head.zScale = scale;
        crown.hat.xScale = scale;
        crown.hat.yScale = scale;
        crown.hat.zScale = scale;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void copyPlayerHeadPose(HumanoidModel<?> playerModel, HumanoidModel<LivingEntity> crown) {
        playerModel.copyPropertiesTo((HumanoidModel) crown);
        crown.head.copyFrom(playerModel.head);
        crown.hat.copyFrom(playerModel.hat);
    }
}
