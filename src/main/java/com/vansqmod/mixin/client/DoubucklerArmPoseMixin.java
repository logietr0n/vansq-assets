package com.vansqmod.mixin.client;

import com.vansqmod.compat.DoubucklerHands;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerRenderer.class)
public abstract class DoubucklerArmPoseMixin {

    @Redirect(
            method = "getArmPose",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;getUsedItemHand()Lnet/minecraft/world/InteractionHand;"
            )
    )
    private static InteractionHand vansqmod$animHand(AbstractClientPlayer player) {
        return DoubucklerHands.animationHand(player);
    }
}
