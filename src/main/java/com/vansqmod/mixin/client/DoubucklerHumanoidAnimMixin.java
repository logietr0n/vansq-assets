package com.vansqmod.mixin.client;

import com.vansqmod.compat.DoubucklerHands;
import com.vansqmod.compat.DoubucklerState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tallestred.piglinproliferation.common.items.BucklerItem;

/**
 * Piglin Proliferation poses whichever hand has {@code isReady}. Doubuckler
 * dashes share the mainhand stack, so that always looks like a mainhand dash
 * until we overwrite the arms after their mixin.
 */
@Mixin(value = HumanoidModel.class, priority = 500)
public abstract class DoubucklerHumanoidAnimMixin {

    private static final float DASH_Y = 1.1466813F;
    private static final float DASH_X = 1.5F;

    @Shadow
    public ModelPart leftArm;

    @Shadow
    public ModelPart rightArm;

    @Redirect(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getUsedItemHand()Lnet/minecraft/world/InteractionHand;"
            )
    )
    private InteractionHand vansqmod$animHand(LivingEntity entity) {
        if (entity instanceof Player player) {
            return DoubucklerHands.animationHand(player);
        }
        return entity.getUsedItemHand();
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At("RETURN")
    )
    private void vansqmod$oneHandDash(
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        if (!(entity instanceof Player player) || !DoubucklerHands.holdingInMain(player)) {
            return;
        }
        int anim = DoubucklerHands.get(player).animHand();
        if (anim == DoubucklerState.ANIM_NONE) {
            return;
        }
        InteractionHand hand = DoubucklerHands.animationHand(player);
        boolean dashRight = hand == InteractionHand.MAIN_HAND
                ? player.getMainArm() == HumanoidArm.RIGHT
                : player.getMainArm() == HumanoidArm.LEFT;
        ItemStack main = player.getMainHandItem();
        boolean dashing = BucklerItem.isReady(main) || BucklerItem.getChargeTicks(main) > 0;
        if (dashing) {
            vansqmod$poseDash(dashRight);
            vansqmod$restoreWalk(dashRight ? this.leftArm : this.rightArm, !dashRight, limbSwing, limbSwingAmount);
            return;
        }
        if (player.isUsingItem() && DoubucklerHands.isDoubuckler(player.getUseItem())) {
            vansqmod$poseWindup(dashRight, player);
            vansqmod$restoreWalk(dashRight ? this.leftArm : this.rightArm, !dashRight, limbSwing, limbSwingAmount);
        }
    }

    @Unique
    private void vansqmod$poseDash(boolean right) {
        ModelPart arm = right ? this.rightArm : this.leftArm;
        arm.xRot = right ? -DASH_X : arm.xRot * 0.1F - DASH_X;
        arm.yRot = right ? -DASH_Y : DASH_Y;
    }

    @Unique
    private void vansqmod$poseWindup(boolean right, LivingEntity entity) {
        ModelPart arm = right ? this.rightArm : this.leftArm;
        float duration = entity.getUseItem().getUseDuration(entity);
        if (duration <= 0.0F) {
            return;
        }
        float progress = Mth.clamp(entity.getTicksUsingItem(), 0.0F, duration) / duration;
        float targetY = right ? -DASH_Y : DASH_Y;
        arm.yRot = Mth.lerp(progress, arm.yRot, targetY);
        arm.xRot = Mth.lerp(progress, arm.xRot, arm.xRot * 0.1F - DASH_X);
    }

    @Unique
    private static void vansqmod$restoreWalk(ModelPart arm, boolean right, float limbSwing, float limbSwingAmount) {
        arm.xRot = Mth.cos(limbSwing * 0.6662F + (right ? Mth.PI : 0.0F)) * 2.0F * limbSwingAmount * 0.5F;
        arm.yRot = 0.0F;
        arm.zRot = 0.0F;
    }
}
