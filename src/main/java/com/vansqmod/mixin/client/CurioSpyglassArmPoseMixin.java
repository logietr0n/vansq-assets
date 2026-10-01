package com.vansqmod.mixin.client;

import com.vansqmod.client.CurioSpyglassPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Curio hotkey scoping never puts a spyglass in a hand, so {@code getArmPose}
 * stays empty and the raise-to-eye animation does not run.
 */
@Mixin(PlayerRenderer.class)
public abstract class CurioSpyglassArmPoseMixin {

    @Inject(method = "getArmPose", at = @At("RETURN"), cancellable = true)
    private static void vansqmod$curioSpyglassPose(
            AbstractClientPlayer player,
            InteractionHand hand,
            CallbackInfoReturnable<HumanoidModel.ArmPose> cir
    ) {
        if (CurioSpyglassPose.poseHand(player, hand)) {
            cir.setReturnValue(HumanoidModel.ArmPose.SPYGLASS);
        }
    }
}
