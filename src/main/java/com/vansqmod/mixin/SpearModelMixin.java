package com.vansqmod.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class SpearModelMixin {

    @Shadow @Final
    private ItemModelShaper itemModelShaper;

    @Unique private ItemStack stack;
    @Unique private ItemDisplayContext context;

    @Unique
    private static final ResourceLocation ROSE_GOLD_ID =
            ResourceLocation.withDefaultNamespace("rose_gold_spear");

    @Unique
    private static final ResourceLocation SILVER_SPEAR_ID =
            ResourceLocation.withDefaultNamespace("silver_spear");

    @Unique
    private static final ResourceLocation NECROMIUM_SPEAR_ID =
            ResourceLocation.withDefaultNamespace("necromium_spear");

    @Unique
    private static final ModelResourceLocation ROSE_GOLD_MODEL =
            ModelResourceLocation.inventory(
                    ResourceLocation.withDefaultNamespace("rose_gold_spear"));

    @Unique
    private static final ModelResourceLocation ROSE_GOLD_MODEL_HAND =
            ModelResourceLocation.inventory(
                    ResourceLocation.withDefaultNamespace("rose_gold_spear_in_hand"));

    @Unique
    private static final ModelResourceLocation SILVER_SPEAR_MODEL =
            ModelResourceLocation.inventory(
                    ResourceLocation.withDefaultNamespace("silver_spear"));

    @Unique
    private static final ModelResourceLocation SILVER_SPEAR_MODEL_HAND =
            ModelResourceLocation.inventory(
                    ResourceLocation.withDefaultNamespace("silver_spear_in_hand"));

    @Unique
    private static final ModelResourceLocation NECROMIUM_SPEAR_MODEL =
            ModelResourceLocation.inventory(
                    ResourceLocation.withDefaultNamespace("necromium_spear"));

    @Unique
    private static final ModelResourceLocation NECROMIUM_SPEAR_MODEL_HAND =
            ModelResourceLocation.inventory(
                    ResourceLocation.withDefaultNamespace("necromium_spear_in_hand"));

    @Unique
    private static final ResourceLocation SCYTHE_ID =
            ResourceLocation.fromNamespaceAndPath("vansqmod", "scythe");

    @Unique
    private static final ModelResourceLocation SCYTHE_MODEL =
            ModelResourceLocation.inventory(
                    ResourceLocation.fromNamespaceAndPath("vansqmod", "scythe"));

    @Unique
    private static final ModelResourceLocation SCYTHE_MODEL_HAND =
            ModelResourceLocation.inventory(
                    ResourceLocation.fromNamespaceAndPath("vansqmod", "scythe_in_hand"));

    @Inject(method="render", at=@At("HEAD"))
    private void capture(ItemStack s, ItemDisplayContext c, boolean bl,
                         PoseStack poseStack, MultiBufferSource buffer,
                         int light, int overlay, BakedModel bakedModel,
                         CallbackInfo ci) {
        this.stack = s;
        this.context = c;
    }

    @ModifyVariable(
            method="render",
            at=@At(value="INVOKE",
                    target="Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
                    shift=At.Shift.AFTER),
            index=8
    )
    private BakedModel replace(BakedModel model) {

        if (stack == null || stack.isEmpty()) return model;

        var key = stack.getItemHolder().unwrapKey();
        if (key.isEmpty()) return model;

        ResourceLocation id = key.get().location();
        boolean inventory =
                context == ItemDisplayContext.GUI ||
                        context == ItemDisplayContext.GROUND ||
                        context == ItemDisplayContext.FIXED;

        if (id.equals(ROSE_GOLD_ID)) {
            if (inventory) {
                return itemModelShaper.getModelManager().getModel(ROSE_GOLD_MODEL);
            }
            return itemModelShaper.getModelManager().getModel(ROSE_GOLD_MODEL_HAND);
        }
        if (id.equals(SILVER_SPEAR_ID)) {
            if (inventory) {
                return itemModelShaper.getModelManager().getModel(SILVER_SPEAR_MODEL);
            }
            return itemModelShaper.getModelManager().getModel(SILVER_SPEAR_MODEL_HAND);
        }
        if (id.equals(NECROMIUM_SPEAR_ID)) {
            if (inventory) {
                return itemModelShaper.getModelManager().getModel(NECROMIUM_SPEAR_MODEL);
            }
            return itemModelShaper.getModelManager().getModel(NECROMIUM_SPEAR_MODEL_HAND);
        }
        if (id.equals(SCYTHE_ID)) {
            if (inventory) {
                return itemModelShaper.getModelManager().getModel(SCYTHE_MODEL);
            }
            return itemModelShaper.getModelManager().getModel(SCYTHE_MODEL_HAND);
        }
        return model;
    }
}