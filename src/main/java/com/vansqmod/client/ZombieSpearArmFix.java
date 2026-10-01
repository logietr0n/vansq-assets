package com.vansqmod.client;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import zzik2.barched.BarchedClient;
import zzik2.barched.minecraft.client.model.effects.SpearAnimations;

/**
 * Barched's zombie model mixin assigns the spear aim pose to {@code arm.getOpposite()}
 * while idle, then clears SPEAR on the hand that actually holds the spear. That
 * raises the empty arm and leaves the spear hanging. Re-apply Barched's own spear
 * aim on the holding arm and hang the other.
 */
public final class ZombieSpearArmFix {

    private ZombieSpearArmFix() {
    }

    public static void fixSpearHands(
            HumanoidModel<?> model,
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks
    ) {
        boolean spearRight = SpearItemHold.isSpearInArm(entity, HumanoidArm.RIGHT);
        boolean spearLeft = SpearItemHold.isSpearInArm(entity, HumanoidArm.LEFT);
        if (!spearRight && !spearLeft) {
            return;
        }
        if (spearRight) {
            model.rightArmPose = BarchedClient.ArmPose.SPEAR;
            poseSpearArm(model.rightArm, model.head, true, entity);
        } else {
            model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
            hangArm(model.rightArm, true, entity, limbSwing, limbSwingAmount, ageInTicks);
        }
        if (spearLeft) {
            model.leftArmPose = BarchedClient.ArmPose.SPEAR;
            poseSpearArm(model.leftArm, model.head, false, entity);
        } else {
            model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
            hangArm(model.leftArm, false, entity, limbSwing, limbSwingAmount, ageInTicks);
        }
    }

    public static void poseSpearArm(ModelPart arm, ModelPart head, boolean right, LivingEntity entity) {
        SpearAnimations.thirdPersonHandUse(
                arm,
                head,
                right,
                SpearItemHold.inArm(entity, right ? HumanoidArm.RIGHT : HumanoidArm.LEFT),
                entity
        );
    }

    public static void hangArm(
            ModelPart arm,
            boolean right,
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks
    ) {
        arm.yRot = 0.0F;
        arm.zRot = 0.0F;
        arm.xRot = right
                ? Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F
                : Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
        if (entity.isPassenger()) {
            arm.xRot += (float) (-Math.PI / 5);
        }
        AnimationUtils.bobModelPart(arm, ageInTicks, right ? 1.0F : -1.0F);
    }
}
