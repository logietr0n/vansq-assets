package com.vansqmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vansqmod.entity.Putrid;
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

/**
 * Renders whatever Putrid is holding (mainhand weapon/fishing rod, offhand
 * copper_lantern) in its hands -- like {@link PutridArmorLayer}, GeckoLib's
 * {@code GeoEntityRenderer} has no built-in equivalent of vanilla's
 * {@code ItemInHandLayer}, so without this the item would be equipped
 * (functionally) but never drawn.
 *
 * <p>Two rig mismatches vs a vanilla zombie/player, both from the arms:
 * <ol>
 *     <li>{@code GeoEntityRenderer} does <em>not</em> apply LivingEntityRenderer's
 *     {@code scale(-1,-1,1)}. Armor copies that flip locally; held items must too,
 *     or vanilla's {@code -90 X / 180 Y} handheld pose (and Amendments' lantern
 *     {@code Z180}) run in the wrong handedness.</li>
 *     <li>BlockBench arm {@code +Y} points at the shoulder. Vanilla {@code ModelPart}
 *     {@code +Y} points at the fingers. After the {@code Z180} flip in (1), that
 *     matches and vanilla handheld rotations apply unchanged — including
 *     {@link PutridModel#ARM_DOWNWARD_TILT_DEGREES} already on the parent arm.</li>
 * </ol>
 */
public class PutridItemLayer extends BlockAndItemGeoLayer<Putrid> {

    private static final String LEFT_HAND = "left_hand";
    private static final String RIGHT_HAND = "right_hand";

    /**
     * Amendments' {@code lantern_item_size} (pack config and mod default are both
     * 0.625). Without this the baked block is drawn at full 1×1×1.
     */
    private static final float LANTERN_HOLDING_SIZE = 0.625F;
    /** Amendments' third-person drop so the handle sits in the fist. */
    private static final float LANTERN_HANG_Y = -0.1875F;
    /** Extra 1px hang (Amendments {@code SPECIAL_OFFSETS} for C&C copper lanterns). */
    private static final float LANTERN_HANG_EXTRA_Y = -1.0F / 16.0F;
    /**
     * Geo arm {@code +Y} points at the shoulder; the fist locator sits 1px toward
     * the elbow from the visual grip. Shift toward the fingers so items don't sit
     * a pixel back in the hand.
     */
    private static final float ITEM_FORWARD_Y = -1.0F / 16.0F;

    public PutridItemLayer(GeoRenderer<Putrid> renderer) {
        super(renderer, PutridItemLayer::stackForBone, (bone, animatable) -> null);
    }

    @Nullable
    private static ItemStack stackForBone(GeoBone bone, Putrid putrid) {
        boolean offhandIsLeftArm = putrid.getMainArm() != HumanoidArm.LEFT;
        ItemStack stack = switch (bone.getName()) {
            case LEFT_HAND -> offhandIsLeftArm ? putrid.getOffhandItem() : putrid.getMainHandItem();
            case RIGHT_HAND -> offhandIsLeftArm ? putrid.getMainHandItem() : putrid.getOffhandItem();
            default -> ItemStack.EMPTY;
        };
        return stack.isEmpty() ? null : stack;
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, Putrid animatable) {
        return LEFT_HAND.equals(bone.getName())
                ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    @Override
    protected void renderStackForBone(
            PoseStack poseStack,
            GeoBone bone,
            ItemStack stack,
            Putrid animatable,
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
        // Same flip LivingEntityRenderer applies before ItemInHandLayer, then vanilla
        // handheld pose. Already at the fist, so skip vanilla's -0.625 shoulder walk.
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        // Vanilla ItemInHandLayer offsets ±1px to the outside of the fist. That
        // reads as "held in the fingers" for swords, but a fishing rod is a
        // centered stick and sits beside the arm instead of in the middle of it.
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

    /**
     * Standing lantern block, hanging world-down from the fist. Undo the arm's
     * pitch in GeckoLib space (where it was applied), then drop the handle into
     * the hand. Do not copy Amendments' {@code Z180}: that flip exists because
     * vanilla already Y-flipped the entity, which this renderer never does.
     */
    private void renderHangingLantern(
            PoseStack poseStack,
            GeoBone hand,
            ItemStack stack,
            Putrid animatable,
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
