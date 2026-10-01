package com.vansqmod.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vansqmod.client.CurioSpyglassPose;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The raised hand has to show a spyglass. An empty main hand would skip the
 * whole held-item pass, so curio scoping keeps that pass alive and substitutes
 * the item on the main arm.
 */
@Mixin(ItemInHandLayer.class)
public abstract class CurioSpyglassHandItemMixin {

    @Unique
    private LivingEntity vansqmod$rendering;

    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD")
    )
    private void vansqmod$captureEntity(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        this.vansqmod$rendering = entity;
    }

    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("RETURN")
    )
    private void vansqmod$clearEntity(CallbackInfo ci) {
        this.vansqmod$rendering = null;
    }

    @Redirect(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
            )
    )
    private boolean vansqmod$renderWhileScoping(ItemStack stack) {
        if (this.vansqmod$rendering instanceof Player player && CurioSpyglassPose.isPosing(player)) {
            return false;
        }
        return stack.isEmpty();
    }

    @ModifyVariable(method = "renderArmWithItem", at = @At("HEAD"), argsOnly = true)
    private ItemStack vansqmod$spyglassInMainHand(
            ItemStack stack,
            LivingEntity entity,
            ItemStack stackArg,
            ItemDisplayContext context,
            HumanoidArm arm
    ) {
        if (entity instanceof Player player && CurioSpyglassPose.poseArm(player, arm)) {
            return Items.SPYGLASS.getDefaultInstance();
        }
        return stack;
    }
}
