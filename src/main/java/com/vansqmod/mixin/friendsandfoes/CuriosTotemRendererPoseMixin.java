package com.vansqmod.mixin.friendsandfoes;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.SlotContext;

/**
 * Friends &amp; Foes mutates the shared Curios pose stack and never pops it,
 * so later curios (spyglass) inherit the totem's scale and chest offset.
 */
@Mixin(
        targets = "com.faboslav.friendsandfoes.neoforge.modcompat.curios.CuriosTotemRenderer",
        remap = false
)
public abstract class CuriosTotemRendererPoseMixin {

    @Inject(method = "render", at = @At("HEAD"), remap = false)
    private <T extends LivingEntity, M extends EntityModel<T>> void vansqmod$pushTotemPose(
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
            float headPitch,
            CallbackInfo ci
    ) {
        poseStack.pushPose();
    }

    @Inject(method = "render", at = @At("RETURN"), remap = false)
    private <T extends LivingEntity, M extends EntityModel<T>> void vansqmod$popTotemPose(
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
            float headPitch,
            CallbackInfo ci
    ) {
        poseStack.popPose();
    }
}
