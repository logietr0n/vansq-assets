package com.vansqmod.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vansqmod.client.DoubucklerClient;
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
public abstract class DoubucklerItemModelMixin {

    @Unique
    private static final ModelResourceLocation vansqmod$IN_HAND =
            ModelResourceLocation.inventory(
                    ResourceLocation.fromNamespaceAndPath("vansqmod", "rose_gold_doubuckler_in_hand"));

    @Unique
    private static final ModelResourceLocation vansqmod$BLOCKING =
            ModelResourceLocation.inventory(
                    ResourceLocation.fromNamespaceAndPath("vansqmod", "rose_gold_doubuckler_blocking"));

    @Shadow
    @Final
    private ItemModelShaper itemModelShaper;

    @Unique
    private ItemStack vansqmod$stack;

    @Unique
    private ItemDisplayContext vansqmod$context;

    @Inject(method = "render", at = @At("HEAD"))
    private void vansqmod$captureDoubuckler(
            ItemStack stack,
            ItemDisplayContext context,
            boolean leftHand,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay,
            BakedModel model,
            CallbackInfo ci
    ) {
        this.vansqmod$stack = stack;
        this.vansqmod$context = context;
    }

    @ModifyVariable(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
                    shift = At.Shift.AFTER
            ),
            index = 8
    )
    private BakedModel vansqmod$heldDoubucklerModel(BakedModel model) {
        ItemStack stack = this.vansqmod$stack;
        ItemDisplayContext context = this.vansqmod$context;
        if (stack == null || !DoubucklerClient.useInHandModel(stack, context)) {
            return model;
        }
        ModelResourceLocation id = DoubucklerClient.showBlockingModel(stack, context)
                ? vansqmod$BLOCKING
                : vansqmod$IN_HAND;
        return this.itemModelShaper.getModelManager().getModel(id);
    }
}
