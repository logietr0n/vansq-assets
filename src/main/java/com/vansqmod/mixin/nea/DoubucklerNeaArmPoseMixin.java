package com.vansqmod.mixin.nea;

import com.vansqmod.compat.DoubucklerHands;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "dev.tr7zw.notenoughanimations.util.AnimationUtil", remap = false)
public abstract class DoubucklerNeaArmPoseMixin {

    @Redirect(
            method = "getArmPose",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;getUsedItemHand()Lnet/minecraft/world/InteractionHand;",
                    remap = true
            )
    )
    private static InteractionHand vansqmod$animHand(AbstractClientPlayer player) {
        return DoubucklerHands.animationHand(player);
    }
}
