package com.vansqmod.mixin.nea;

import com.vansqmod.client.CurioSpyglassPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Not Enough Animations rebuilds arm poses from the held item, which drops the
 * curio-hotkey spyglass pose set on {@code PlayerRenderer}.
 */
@Mixin(targets = "dev.tr7zw.notenoughanimations.util.AnimationUtil", remap = false)
public abstract class CurioSpyglassNeaArmPoseMixin {

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
