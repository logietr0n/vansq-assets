package com.vansqmod.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vansqmod.compat.DoubucklerHands;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class DoubucklerOffhandRenderMixin {

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void vansqmod$pushRenderPlayer(
            AbstractClientPlayer player,
            float partialTicks,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack stack,
            float equippedProgress,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            CallbackInfo ci
    ) {
        DoubucklerHands.pushRenderPlayer(player);
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"))
    private void vansqmod$popRenderPlayer(CallbackInfo ci) {
        DoubucklerHands.popRenderPlayer();
    }

    @ModifyVariable(
            method = "renderArmWithItem",
            at = @At("HEAD"),
            argsOnly = true
    )
    private ItemStack vansqmod$offhandCopy(
            ItemStack stack,
            AbstractClientPlayer player,
            float partialTicks,
            float pitch,
            InteractionHand hand
    ) {
        if (hand == InteractionHand.OFF_HAND) {
            return DoubucklerHands.displayOffhand(player);
        }
        return stack;
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getOffhandItem()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack vansqmod$tickOffhandCopy(LocalPlayer player) {
        return DoubucklerHands.displayOffhand(player);
    }

    @Redirect(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;getUsedItemHand()Lnet/minecraft/world/InteractionHand;"
            )
    )
    private InteractionHand vansqmod$animHand(AbstractClientPlayer player) {
        return DoubucklerHands.animationHand(player);
    }

    @Redirect(
            method = "evaluateWhichHandsToRender",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getOffhandItem()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private static ItemStack vansqmod$evalOffhandCopy(LocalPlayer player) {
        return DoubucklerHands.displayOffhand(player);
    }
}
