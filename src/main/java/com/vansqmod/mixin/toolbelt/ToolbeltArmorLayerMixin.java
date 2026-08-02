package com.vansqmod.mixin.toolbelt;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vansqmod.integration.toolbelt.ToolbeltEquipment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Renders leggings from the legs slot and the toolbelt from the Curios belt slot (on top).
 */
@Mixin(targets = "net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer")
public abstract class ToolbeltArmorLayerMixin {

    @Unique
    private boolean vansqmod$toolbeltPass;

    @Shadow
    protected abstract void renderArmorPiece(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            LivingEntity entity,
            EquipmentSlot slot,
            int packedLight,
            HumanoidModel<?> model,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    );

    @Redirect(
            method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack vansqmod$resolveLegSlotStack(LivingEntity entity, EquipmentSlot slot) {
        if (slot != EquipmentSlot.LEGS) {
            return entity.getItemBySlot(slot);
        }
        if (vansqmod$toolbeltPass) {
            return ToolbeltEquipment.getEquippedToolbelt(entity).orElse(ItemStack.EMPTY);
        }
        ItemStack legs = entity.getItemBySlot(EquipmentSlot.LEGS);
        return ToolbeltEquipment.isToolbelt(legs) ? ItemStack.EMPTY : legs;
    }

    @Inject(
            method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",
            at = @At("TAIL")
    )
    private void vansqmod$renderToolbeltOverLeggings(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            LivingEntity entity,
            EquipmentSlot slot,
            int packedLight,
            HumanoidModel<?> model,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        if (vansqmod$toolbeltPass || slot != EquipmentSlot.LEGS) {
            return;
        }
        if (ToolbeltEquipment.getEquippedToolbelt(entity).isEmpty()) {
            return;
        }
        vansqmod$toolbeltPass = true;
        try {
            this.renderArmorPiece(
                    poseStack,
                    bufferSource,
                    entity,
                    EquipmentSlot.LEGS,
                    packedLight,
                    model,
                    limbSwing,
                    limbSwingAmount,
                    partialTick,
                    ageInTicks,
                    netHeadYaw,
                    headPitch
            );
        } finally {
            vansqmod$toolbeltPass = false;
        }
    }
}
