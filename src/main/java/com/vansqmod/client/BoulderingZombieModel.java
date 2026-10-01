package com.vansqmod.client;

import com.vansqmod.VansqMod;
import com.vansqmod.entity.BoulderingZombie;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.fml.ModList;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/**
 * Vanilla zombie posing on the copied zombie geo, plus Not Enough Animations'
 * player ladder pose while {@link BoulderingZombie#isClimbing()}.
 */
public class BoulderingZombieModel extends DefaultedEntityGeoModel<BoulderingZombie> {

    private static final ResourceLocation ADULT_ID =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "bouldering_zombie");
    private static final ResourceLocation BABY_ID =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "bouldering_zombie_baby");

    private static final float DEFAULT_LADDER_AMPLIFIER = 0.35F;
    private static final float DEFAULT_LADDER_ARM_HEIGHT = 1.7F;
    private static final float DEFAULT_LADDER_ARM_SPEED = 2.0F;

    private final ResourceLocation babyModel;
    private final ResourceLocation babyTexture;

    public BoulderingZombieModel() {
        super(ADULT_ID, true);
        this.babyModel = buildFormattedModelPath(BABY_ID);
        this.babyTexture = buildFormattedTexturePath(BABY_ID);
    }

    @Override
    public ResourceLocation getModelResource(BoulderingZombie animatable) {
        return animatable.isBaby() ? this.babyModel : super.getModelResource(animatable);
    }

    @Override
    public ResourceLocation getTextureResource(BoulderingZombie animatable) {
        return animatable.isBaby() ? this.babyTexture : super.getTextureResource(animatable);
    }

    @Override
    public void setCustomAnimations(BoulderingZombie zombie, long instanceId, AnimationState<BoulderingZombie> state) {
        super.setCustomAnimations(zombie, instanceId, state);

        getBone("head").ifPresent(head -> {
            head.setScaleX(1.0F);
            head.setScaleY(1.0F);
            head.setScaleZ(1.0F);
        });

        float limbSwing = state.getLimbSwing();
        float limbSwingAmount = state.getLimbSwingAmount();
        float ageInTicks = (float) zombie.tickCount + state.getPartialTick();
        getBone("body").ifPresent(body -> body.setRotY(0.0F));

        if (zombie.isClimbing()) {
            applyClimbingPose(zombie);
            return;
        }

        boolean riding = zombie.isPassenger();
        getBone("right_leg").ifPresent(rightLeg -> {
            if (riding) {
                rightLeg.setRotX(1.4137167F);
                rightLeg.setRotY((float) -Math.PI / 10.0F);
                rightLeg.setRotZ(-0.07853982F);
            } else {
                rightLeg.setRotX(-(Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount));
                rightLeg.setRotY(0.005F);
                rightLeg.setRotZ(0.005F);
            }
        });
        getBone("left_leg").ifPresent(leftLeg -> {
            if (riding) {
                leftLeg.setRotX(1.4137167F);
                leftLeg.setRotY((float) Math.PI / 10.0F);
                leftLeg.setRotZ(0.07853982F);
            } else {
                leftLeg.setRotX(-(Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount));
                leftLeg.setRotY(-0.005F);
                leftLeg.setRotZ(-0.005F);
            }
        });

        float attackTime = zombie.getAttackAnim(state.getPartialTick());
        boolean aggressive = zombie.isAggressive();
        float f = Mth.sin(attackTime * (float) Math.PI);
        float f1 = Mth.sin((1.0F - (1.0F - attackTime) * (1.0F - attackTime)) * (float) Math.PI);
        float reachPitch = (float) -Math.PI / (aggressive ? 1.5F : 2.25F);
        boolean rightSpear = SpearItemHold.isSpearInArm(zombie, HumanoidArm.RIGHT);
        boolean leftSpear = SpearItemHold.isSpearInArm(zombie, HumanoidArm.LEFT);
        float headPitch = getBone("head").map(GeoBone::getRotX).orElse(0.0F);
        float headYaw = getBone("head").map(GeoBone::getRotY).orElse(0.0F);

        getBone("right_arm").ifPresent(rightArm -> {
            if (rightSpear) {
                applySpearAimPose(rightArm, headPitch, headYaw, true);
                return;
            }
            if (leftSpear) {
                hangArm(rightArm, true, limbSwing, limbSwingAmount, ageInTicks);
                return;
            }
            rightArm.setRotZ(0.0F);
            rightArm.setRotY(-(0.1F - f * 0.6F));
            rightArm.setRotX(-(reachPitch + f * 1.2F - f1 * 0.4F));
            bobBone(rightArm, ageInTicks, 1.0F, -1.0F);
        });
        getBone("left_arm").ifPresent(leftArm -> {
            if (leftSpear) {
                applySpearAimPose(leftArm, headPitch, headYaw, false);
                return;
            }
            if (rightSpear) {
                hangArm(leftArm, false, limbSwing, limbSwingAmount, ageInTicks);
                return;
            }
            leftArm.setRotZ(0.0F);
            leftArm.setRotY(0.1F - f * 0.6F);
            leftArm.setRotX(-(reachPitch + f * 1.2F - f1 * 0.4F));
            bobBone(leftArm, ageInTicks, -1.0F, 1.0F);
        });
    }

    /**
     * Not Enough Animations {@code LadderAnimation#apply}, mapped onto this geo rig
     * (vanilla ModelPart pitch is negated, left-side yaw/roll stay mirrored).
     */
    private void applyClimbingPose(BoulderingZombie zombie) {
        float[] params = ladderParams();
        float amplifier = params[0];
        float armHeight = params[1];
        float armSpeed = params[2];
        float rotation = -Mth.cos((float) (zombie.getY() * armSpeed)) * amplifier;

        getBone("right_leg").ifPresent(leg -> applyNeaPart(leg, -1.0F - rotation, -0.2F, 0.3F, false));
        getBone("left_leg").ifPresent(leg -> applyNeaPart(leg, -1.0F - (-rotation), -0.2F, 0.3F, true));
        getBone("right_arm").ifPresent(arm -> applyNeaPart(arm, -armHeight - rotation, -0.2F, 0.3F, false));
        getBone("left_arm").ifPresent(arm -> applyNeaPart(arm, -armHeight - (-rotation), -0.2F, 0.3F, true));
    }

    private static void applyNeaPart(GeoBone bone, float pitch, float yaw, float roll, boolean mirror) {
        bone.setRotX(-pitch);
        bone.setRotY(mirror ? -yaw : yaw);
        bone.setRotZ(mirror ? -roll : roll);
    }

    private static float[] ladderParams() {
        float amplifier = DEFAULT_LADDER_AMPLIFIER;
        float armHeight = DEFAULT_LADDER_ARM_HEIGHT;
        float armSpeed = DEFAULT_LADDER_ARM_SPEED;
        if (!ModList.get().isLoaded("notenoughanimations")) {
            return new float[]{amplifier, armHeight, armSpeed};
        }
        try {
            Class<?> base = Class.forName("dev.tr7zw.notenoughanimations.versionless.NEABaseMod");
            Object config = base.getField("config").get(null);
            if (config != null) {
                amplifier = config.getClass().getField("ladderAnimationAmplifier").getFloat(config);
                armHeight = config.getClass().getField("ladderAnimationArmHeight").getFloat(config);
                armSpeed = config.getClass().getField("ladderAnimationArmSpeed").getFloat(config);
            }
        } catch (ReflectiveOperationException ignored) {
            // Keep NEA's shipped defaults.
        }
        return new float[]{amplifier, armHeight, armSpeed};
    }

    private static void applySpearAimPose(GeoBone arm, float headPitch, float headYaw, boolean isRightArm) {
        float dir = isRightArm ? 1.0F : -1.0F;
        float yRot = -0.1F * dir + headYaw;
        float xRot = (float) (-Math.PI / 2.0) + headPitch + 0.8F;
        yRot = (float) Math.toRadians(Mth.clamp((float) Math.toDegrees(yRot), -60.0F, 60.0F));
        xRot = (float) Math.toRadians(Mth.clamp((float) Math.toDegrees(xRot), -120.0F, 30.0F));
        arm.setRotX(-xRot);
        arm.setRotY(yRot);
        arm.setRotZ(0.0F);
    }

    private static void hangArm(GeoBone arm, boolean right, float limbSwing, float limbSwingAmount, float ageInTicks) {
        float walk = right
                ? Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F
                : Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
        arm.setRotZ(0.0F);
        arm.setRotY(0.0F);
        arm.setRotX(-walk);
        bobBone(arm, ageInTicks, right ? 1.0F : -1.0F, right ? -1.0F : 1.0F);
    }

    private static void bobBone(GeoBone bone, float ageInTicks, float zMod, float xMod) {
        bone.setRotZ(bone.getRotZ() + zMod * (Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F));
        bone.setRotX(bone.getRotX() + xMod * Mth.sin(ageInTicks * 0.067F) * 0.05F);
    }
}
