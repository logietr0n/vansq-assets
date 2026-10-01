package com.vansqmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Spyglass Improvements registers a Curios renderer, but it bakes the inventory
 * sprite (the player is not "using" the spyglass). Swap in the 3D in-hand model
 * and keep SI's hip placement.
 */
public final class SpyglassCurioRenderer implements ICurioRenderer {

    private static final ModelResourceLocation IN_HAND_MODEL =
            ModelResourceLocation.inventory(ResourceLocation.withDefaultNamespace("spyglass_in_hand"));

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
        LivingEntity entity = slotContext.entity();
        if (entity instanceof Player player && CurioSpyglassPose.isPosing(player)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        BakedModel model = minecraft.getModelManager().getModel(IN_HAND_MODEL);
        if (model == null || model == minecraft.getModelManager().getMissingModel()) {
            model = itemRenderer.getModel(stack, minecraft.level, minecraft.player, 1);
        }

        poseStack.pushPose();
        if (entity.isCrouching()) {
            poseStack.translate(0.0F, 0.15F, 0.32F);
        }
        poseStack.translate(0.16D, 0.6D, 0.16D);
        poseStack.mulPose(Direction.DOWN.getRotation());
        poseStack.scale(0.7F, 0.7F, 0.7F);
        itemRenderer.render(
                stack,
                ItemDisplayContext.NONE,
                true,
                poseStack,
                buffer,
                light,
                OverlayTexture.NO_OVERLAY,
                model
        );
        poseStack.popPose();
    }
}
