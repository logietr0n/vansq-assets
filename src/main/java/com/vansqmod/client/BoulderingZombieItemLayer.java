package com.vansqmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vansqmod.entity.BoulderingZombie;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import javax.annotation.Nullable;

public class BoulderingZombieItemLayer extends BlockAndItemGeoLayer<BoulderingZombie> {

    private static final String LEFT_HAND = "left_hand";
    private static final String RIGHT_HAND = "right_hand";
    private static final float LANTERN_HOLDING_SIZE = 0.625F;
    private static final float LANTERN_HANG_Y = -0.1875F;
    private static final float LANTERN_HANG_EXTRA_Y = -1.0F / 16.0F;
    private static final float ITEM_FORWARD_Y = -1.0F / 16.0F;

    public BoulderingZombieItemLayer(GeoRenderer<BoulderingZombie> renderer) {
        super(renderer, BoulderingZombieItemLayer::stackForBone, (bone, animatable) -> null);
    }

    @Nullable
    private static ItemStack stackForBone(GeoBone bone, BoulderingZombie zombie) {
        boolean offhandIsLeftArm = zombie.getMainArm() != HumanoidArm.LEFT;
        ItemStack stack = switch (bone.getName()) {
            case LEFT_HAND -> offhandIsLeftArm ? zombie.getOffhandItem() : zombie.getMainHandItem();
            case RIGHT_HAND -> offhandIsLeftArm ? zombie.getMainHandItem() : zombie.getOffhandItem();
            default -> ItemStack.EMPTY;
        };
        return stack.isEmpty() ? null : stack;
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, BoulderingZombie animatable) {
        return LEFT_HAND.equals(bone.getName())
                ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    @Override
    protected void renderStackForBone(
            PoseStack poseStack,
            GeoBone bone,
            ItemStack stack,
            BoulderingZombie animatable,
            MultiBufferSource bufferSource,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        boolean leftHand = LEFT_HAND.equals(bone.getName());
        poseStack.translate(0.0F, ITEM_FORWARD_Y, 0.0F);
        if (isLantern(stack)) {
            renderHangingLantern(poseStack, bone, stack, animatable, bufferSource, packedLight, leftHand);
            return;
        }
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        float side = stack.getItem() instanceof FishingRodItem
                ? 0.0F
                : (leftHand ? -1.0F / 16.0F : 1.0F / 16.0F);
        poseStack.translate(side, 0.125F, 0.0F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                animatable,
                stack,
                getTransformTypeForStack(bone, stack, animatable),
                leftHand,
                poseStack,
                bufferSource,
                animatable.level(),
                packedLight,
                packedOverlay,
                animatable.getId());
    }

    private static boolean isLantern(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock().defaultBlockState().hasProperty(LanternBlock.HANGING);
    }

    private void renderHangingLantern(
            PoseStack poseStack,
            GeoBone hand,
            ItemStack stack,
            BoulderingZombie animatable,
            MultiBufferSource bufferSource,
            int packedLight,
            boolean leftHand
    ) {
        GeoBone arm = hand.getParent();
        if (arm != null) {
            poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(arm.getRotZ())));
            poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.toDegrees(-arm.getRotX())));
        }
        poseStack.scale(LANTERN_HOLDING_SIZE, LANTERN_HOLDING_SIZE, LANTERN_HOLDING_SIZE);
        poseStack.translate(0.0F, LANTERN_HANG_Y + LANTERN_HANG_EXTRA_Y, 0.0F);

        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }
        BlockState state = blockItem.getBlock().defaultBlockState();
        if (state.hasProperty(LanternBlock.HANGING)) {
            state = state.setValue(LanternBlock.HANGING, false);
        }
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        BakedModel model = minecraft.getBlockRenderer().getBlockModel(state);
        itemRenderer.render(
                stack,
                ItemDisplayContext.NONE,
                leftHand,
                poseStack,
                bufferSource,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                model);
    }
}
