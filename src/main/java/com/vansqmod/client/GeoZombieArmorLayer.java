package com.vansqmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;

import javax.annotation.Nullable;

/**
 * Draws vanilla armor on geo zombie bones. GeckoLib's {@code GeoEntityRenderer}
 * has no {@code HumanoidArmorLayer}; without this, spawn armor is stats-only.
 *
 * <p>{@code GeoRenderer#renderRecursively} already applies the bone's pivot
 * rotation before this layer runs. GeckoLib's default
 * {@code prepModelPartForRender} copies that rotation onto the vanilla
 * {@code ModelPart}, so armor would swing twice. Rotations are cleared after
 * the default pivot/scale copy. Leggings also draw their inner-armor waist
 * on {@code body}; GeckoLib only allows one stack per bone, so chestplate
 * would otherwise hide that torso piece.</p>
 *
 * <p>Babies use Tiny Takeover's unique armor mesh and
 * {@code textures/models/armor/baby/*.png} sheets instead of adult player
 * {@code layer_1}/{@code layer_2} UVs.</p>
 */
public class GeoZombieArmorLayer<T extends LivingEntity & GeoAnimatable> extends ItemArmorGeoLayer<T> {

    public GeoZombieArmorLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Nullable
    @Override
    protected ItemStack getArmorItemForBone(GeoBone bone, T animatable) {
        return switch (bone.getName()) {
            case "head" -> this.helmetStack;
            case "body", "right_arm", "left_arm" -> this.chestplateStack;
            case "right_leg", "left_leg" ->
                    this.leggingsStack != null && !this.leggingsStack.isEmpty() ? this.leggingsStack : this.bootsStack;
            default -> null;
        };
    }

    @Override
    protected ModelPart getModelPartForBone(
            GeoBone bone, EquipmentSlot slot, ItemStack stack, T animatable, HumanoidModel<?> baseModel
    ) {
        return BabyZombieGeoArmor.partFor(animatable.isBaby(), bone.getName(), slot, baseModel);
    }

    @Override
    protected HumanoidModel<?> getModelForItem(GeoBone bone, EquipmentSlot slot, ItemStack stack, T animatable) {
        return BabyZombieGeoArmor.modelFor(animatable.isBaby(), slot, super.getModelForItem(bone, slot, stack, animatable));
    }

    @Override
    protected VertexConsumer getVanillaArmorBuffer(
            MultiBufferSource bufferSource,
            T animatable,
            ItemStack stack,
            EquipmentSlot slot,
            GeoBone bone,
            ArmorMaterial.Layer layer,
            int packedLight,
            int packedOverlay,
            boolean forGlint
    ) {
        if (forGlint) {
            return bufferSource.getBuffer(RenderType.armorEntityGlint());
        }
        ResourceLocation texture = layer.texture(slot == EquipmentSlot.LEGS);
        if (animatable.isBaby()) {
            texture = BabyZombieGeoArmor.remapLayerTexture(texture);
        }
        return bufferSource.getBuffer(RenderType.armorCutoutNoCull(texture));
    }

    @Override
    public void renderForBone(
            PoseStack poseStack,
            T animatable,
            GeoBone bone,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        super.renderForBone(poseStack, animatable, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        if (!"body".equals(bone.getName())
                || this.leggingsStack == null
                || this.leggingsStack.isEmpty()
                || !(this.leggingsStack.getItem() instanceof ArmorItem)) {
            return;
        }
        renderArmorOnBone(
                poseStack,
                animatable,
                bone,
                EquipmentSlot.LEGS,
                this.leggingsStack,
                bufferSource,
                partialTick,
                packedLight,
                packedOverlay
        );
    }

    private void renderArmorOnBone(
            PoseStack poseStack,
            T animatable,
            GeoBone bone,
            EquipmentSlot slot,
            ItemStack armorStack,
            MultiBufferSource bufferSource,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        HumanoidModel<?> model = this.getModelForItem(bone, slot, armorStack, animatable);
        ModelPart modelPart = this.getModelPartForBone(bone, slot, armorStack, animatable, model);
        poseStack.pushPose();
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.prepModelPartForRender(poseStack, bone, modelPart);
        this.renderVanillaArmorPiece(
                poseStack, animatable, bone, slot, armorStack, modelPart, bufferSource, partialTick, packedLight, packedOverlay
        );
        poseStack.popPose();
    }

    @Override
    protected void prepModelPartForRender(PoseStack poseStack, GeoBone bone, ModelPart sourcePart) {
        super.prepModelPartForRender(poseStack, bone, sourcePart);
        sourcePart.xRot = 0.0F;
        sourcePart.yRot = 0.0F;
        sourcePart.zRot = 0.0F;
    }
}
