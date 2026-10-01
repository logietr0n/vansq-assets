package com.vansqmod.client;

import com.vansqmod.VansqMod;
import com.vansqmod.entity.Putrid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

/**
 * Fully procedural -- no BlockBench clips. This ports vanilla's own zombie animation
 * code ({@code HumanoidModel#setupAnim} and {@code AbstractZombieModel#setupAnim}'s
 * arm override, both in {@code net.minecraft.client.model}) onto the geo bones 1:1,
 * so Putrid moves exactly like a default {@code Zombie}:
 * <ul>
 *     <li>Legs swing off {@code limbSwing}/{@code limbSwingAmount} (the same
 *     distance-based accumulator vanilla uses), so the stride always matches actual
 *     movement speed instead of looping on a fixed timer.</li>
 *     <li>Arms hold vanilla's fixed "reach forward" pose (tighter when aggressive)
 *     and never swing with footsteps -- vanilla zombie arms don't either -- but get
 *     a continuous idle sway ({@code AnimationUtils#bobModelPart}) layered on top so
 *     they're never static, plus the attack-swing flex while an attack is playing.</li>
 *     <li>Head-look tracking comes from {@code DefaultedEntityGeoModel}'s built-in
 *     {@code turnsHead} handling (called via {@code super}), same as vanilla
 *     {@code ZombieModel}.</li>
 * </ul>
 * Not ported: the vanilla attack-swing body twist / arm lunge
 * (see {@code HumanoidModel#setupAttackAnimation}) -- its rotational contribution to
 * the arms is fully overwritten by the zombie arm override in vanilla too, so the
 * only thing it would still add here is a small sideways arm/body sway during a
 * swing. Left out for now since nothing flagged it as an issue.
 */
public class PutridModel extends DefaultedEntityGeoModel<Putrid> {

    private static final ResourceLocation ADULT_ID =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "putrid");
    private static final ResourceLocation BABY_ID =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "putrid_baby");

    private final ResourceLocation babyModel;
    private final ResourceLocation babyTexture;

    public PutridModel() {
        super(ADULT_ID, true);
        this.babyModel = buildFormattedModelPath(BABY_ID);
        this.babyTexture = buildFormattedTexturePath(BABY_ID);
    }

    @Override
    public ResourceLocation getModelResource(Putrid animatable) {
        return animatable.isBaby() ? this.babyModel : super.getModelResource(animatable);
    }

    @Override
    public ResourceLocation getTextureResource(Putrid animatable) {
        return animatable.isBaby() ? this.babyTexture : super.getTextureResource(animatable);
    }

    /**
     * The actual mushroom growth bones -- these are what shearing removes, same
     * idea as {@code BoggedModel#prepareMobModel} hiding its "mushrooms" part once
     * sheared. {@code body_outer}/{@code head_outer} (and the arm moss sleeves)
     * are a separate, permanent layer and are intentionally not in this list.
     */
    private static final String[] SHROOM_BONES = {"body_shroom", "head_shroom"};

    /**
     * Extra droop vs a vanilla zombie. Held items inherit this from the arm bone
     * as long as they stay in arm-local space — see {@link PutridItemLayer}.
     */
    public static final float ARM_DOWNWARD_TILT_DEGREES = 15.0F;
    private static final float ARM_DOWNWARD_TILT = (float) Math.toRadians(ARM_DOWNWARD_TILT_DEGREES);

    /**
     * While holding a lantern the aim pose still gets {@link #ARM_DOWNWARD_TILT},
     * then this much is added back so the lantern arm sits 10° above the drooped
     * reach rather than matching it.
     */
    public static final float LANTERN_HOLD_UPWARD_DEGREES = 10.0F;
    private static final float LANTERN_HOLD_UPWARD = (float) Math.toRadians(LANTERN_HOLD_UPWARD_DEGREES);

    // --- NotEnoughAnimations' default held-lantern pose, ported 1:1. NEA doesn't
    // swap in a different lantern *model* -- it's the same flat vanilla item render
    // either way -- what it actually does is replace the holding arm's pose with a
    // "hold the light up toward wherever you're looking" aim (its
    // LookAtItemAnimation, priority 300, so it fully owns the arm instead of
    // stacking with the normal held-item pose) whenever the held item is in its
    // `holdingItems` list (vanilla's own copper_lantern variants are in that list
    // by default) and holdUpItemsMode/holdUpTarget are left at their defaults
    // (CONFIG / CAMERA). The formula below is NEA's CAMERA-target
    // AnimationUtil#applyArmTransforms(...) call, copied exactly (including its
    // magic constants -- -PI/2 base pitch, [-2.5, 0] pitch clamp, 0.1f default
    // holdUpCameraOffset, [-0.2, 0.1] yaw clamp, 0.1f roll), just re-pointed at
    // this rig's head bone (already posed by the super.setCustomAnimations() head-
    // look call above) instead of NEA's player camera, and with the same rotX-sign
    // reversal the rest of this rig's arms need (see the big comment below).
    private static final float LANTERN_AIM_BASE_PITCH = (float) (-Math.PI / 2.0);
    private static final float LANTERN_AIM_MIN_PITCH = -2.5F;
    private static final float LANTERN_AIM_MAX_PITCH = 0.0F;
    private static final float LANTERN_AIM_CAMERA_OFFSET = 0.1F;
    private static final float LANTERN_AIM_MIN_YAW = -0.2F;
    private static final float LANTERN_AIM_ROLL = 0.1F;

    @Override
    public void setCustomAnimations(Putrid putrid, long instanceId, AnimationState<Putrid> state) {
        // Head-look (netHeadYaw/headPitch), same as vanilla ZombieModel.
        super.setCustomAnimations(putrid, instanceId, state);

        // Unique baby geo is already true-size (Tiny Takeover drowned proportions).
        // Reset any leftover adult-baby 1.5 head scale from a shared bone cache.
        getBone("head").ifPresent(head -> {
            head.setScaleX(1.0F);
            head.setScaleY(1.0F);
            head.setScaleZ(1.0F);
        });

        // Hide the growth bones instead of removing them from the geometry, so
        // they'd come back correctly if the sheared flag were ever reverted.
        boolean sheared = putrid.isSheared();
        for (String boneName : SHROOM_BONES) {
            getBone(boneName).ifPresent(bone -> bone.setHidden(sheared));
        }

        float limbSwing = state.getLimbSwing();
        float limbSwingAmount = state.getLimbSwingAmount();
        float ageInTicks = (float) putrid.tickCount + state.getPartialTick();

        getBone("body").ifPresent(body -> body.setRotY(0.0F));

        // --- Legs: HumanoidModel#setupAnim's walk swing, untouched by the zombie
        // arm override, so this is identical to a regular biped's stride -- except
        // while riding (boat/minecart/etc), where vanilla locks the legs into a
        // fixed "sitting" bend instead. (The same riding block also nudges the arms,
        // but that contribution gets fully overwritten by animateZombieArms below
        // for a zombie, same as the attack flex, so it's correctly left out here.) ---
        // Same rig quirk as the arms below -- this model's pitch (X) axis reaches
        // the opposite way from vanilla's ModelPart convention, so it's negated
        // here too for consistency (even though a symmetric walk swing hides a
        // sign flip much better than the arms' fixed pose did).
        // The riding pose's sideways knee splay (yRot/zRot) came out inward instead
        // of outward, so those are negated here too, same as the pitch (X) already
        // was above -- this rig's yaw/roll sense is also mirrored from vanilla's.
        boolean riding = putrid.isPassenger();
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

        // --- Arms: AnimationUtils#animateZombieArms, called from
        // AbstractZombieModel#setupAnim after (and overwriting) the humanoid
        // walk-swing -- vanilla zombie arms never swing with footsteps. ---
        float attackTime = putrid.getAttackAnim(state.getPartialTick());
        boolean aggressive = putrid.isAggressive();
        float f = Mth.sin(attackTime * (float) Math.PI);
        float f1 = Mth.sin((1.0F - (1.0F - attackTime) * (1.0F - attackTime)) * (float) Math.PI);
        float reachPitch = (float) -Math.PI / (aggressive ? 1.5F : 2.25F);

        // Offhand is left_arm unless this particular zombie rolled left-handed
        // (Mob#finalizeSpawn, 5% chance) -- getMainArm()/getOffhandItem() already
        // account for that, so this stays correct either way.
        boolean offhandIsLeftArm = putrid.getMainArm() != HumanoidArm.LEFT;
        boolean offhandLantern = putrid.isHoldingCopperLantern();
        // NEA's hold-up aim is for standing. While sitting/resting (or any
        // passenger pose) that skyward pitch puts the lantern well above the
        // hunched torso; use the normal reach/droop so it hangs from a lowered fist.
        boolean lanternAim = offhandLantern && !riding;
        boolean rightSpear = SpearItemHold.isSpearInArm(putrid, HumanoidArm.RIGHT);
        boolean leftSpear = SpearItemHold.isSpearInArm(putrid, HumanoidArm.LEFT);

        // Look target for the NEA lantern-aim pose below -- this rig's own head
        // bone, already posed by the super.setCustomAnimations() call up top.
        float headPitch = getBone("head").map(GeoBone::getRotX).orElse(0.0F);
        float headYaw = getBone("head").map(GeoBone::getRotY).orElse(0.0F);

        // NOTE: this geo model's right_arm/left_arm bones reach the *opposite* way
        // from a rotX sign that matches vanilla's own ModelPart convention -- the
        // unmodified vanilla sign put them up behind the head instead of forward.
        // Negated below so the pitch axis matches this particular rig. The constant
        // droop is subtracted (this rig's positive rotX reaches further
        // forward/up, so less of it droops the arm back down).
        //
        // Whichever arm is currently the offhand skips this normal reach/droop/bob
        // pose while standing with the lantern -- same as NEA's own
        // LookAtItemAnimation, which (at its default priority 300) fully replaces
        // the held-item pose rather than adding to it -- and gets the NEA aim pose
        // from applyLanternAimPose(...) instead. Sitting/resting keeps the reach
        // pose so the lantern isn't held up above the folded torso.
        getBone("right_arm").ifPresent(rightArm -> {
            if (lanternAim && !offhandIsLeftArm) {
                applyLanternAimPose(rightArm, headPitch, headYaw, false);
                return;
            }
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
            rightArm.setRotX(-(reachPitch + f * 1.2F - f1 * 0.4F) - ARM_DOWNWARD_TILT);
            bobBone(rightArm, ageInTicks, 1.0F, -1.0F);
        });
        getBone("left_arm").ifPresent(leftArm -> {
            if (lanternAim && offhandIsLeftArm) {
                applyLanternAimPose(leftArm, headPitch, headYaw, true);
                return;
            }
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
            leftArm.setRotX(-(reachPitch + f * 1.2F - f1 * 0.4F) - ARM_DOWNWARD_TILT);
            bobBone(leftArm, ageInTicks, -1.0F, 1.0F);
        });
    }

    /**
     * Barched {@code SpearAnimations#thirdPersonHandUse} idle aim, mapped onto this
     * rig the same way the lantern pose is: vanilla ModelPart pitch is negated,
     * yaw/roll keep vanilla sign.
     */
    private static void applySpearAimPose(GeoBone arm, float headPitch, float headYaw, boolean isRightArm) {
        float dir = isRightArm ? 1.0F : -1.0F;
        float yRot = -0.1F * dir + headYaw;
        float xRot = LANTERN_AIM_BASE_PITCH + headPitch + 0.8F;
        yRot = (float) Math.toRadians(Mth.clamp((float) Math.toDegrees(yRot), -60.0F, 60.0F));
        xRot = (float) Math.toRadians(Mth.clamp((float) Math.toDegrees(xRot), -120.0F, 30.0F));
        arm.setRotX(-xRot - ARM_DOWNWARD_TILT);
        arm.setRotY(yRot);
        arm.setRotZ(0.0F);
    }

    private static void hangArm(GeoBone arm, boolean right, float limbSwing, float limbSwingAmount, float ageInTicks) {
        float walk = right
                ? Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F
                : Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
        arm.setRotZ(0.0F);
        arm.setRotY(0.0F);
        arm.setRotX(-walk - ARM_DOWNWARD_TILT);
        bobBone(arm, ageInTicks, right ? 1.0F : -1.0F, right ? -1.0F : 1.0F);
    }

    /**
     * NotEnoughAnimations' {@code LookAtItemAnimation}, CAMERA-target case, copied
     * onto this rig's arm bone convention -- see the constants block above.
     */
    private static void applyLanternAimPose(GeoBone arm, float headPitch, float headYaw, boolean isLeftArm) {
        float pitch = Mth.clamp(LANTERN_AIM_BASE_PITCH + headPitch, LANTERN_AIM_MIN_PITCH, LANTERN_AIM_MAX_PITCH);
        // This rig's rotX needs sign reversal (see above). The shared arm droop is
        // applied here too, then 10° is added back so a held lantern still sits
        // above the reach pose. Yaw/roll stay at the rig's neutral forward-reach
        // (0); NEA's camera-aim nuance put the lantern on the wrong side / upside down.
        arm.setRotX(-pitch - ARM_DOWNWARD_TILT + LANTERN_HOLD_UPWARD);
        arm.setRotY(0.0F);
        arm.setRotZ(0.0F);
    }

    /**
     * {@code AnimationUtils#bobModelPart}: a small continuous sway independent of
     * movement -- this is vanilla's "breathing" idle motion, applied here to the arms
     * exactly like it is for a regular zombie. {@code zMod}/{@code xMod} are separate
     * so the pitch (X) component can be negated to match this rig's reach direction
     * without touching the roll (Z) component.
     */
    private static void bobBone(GeoBone bone, float ageInTicks, float zMod, float xMod) {
        bone.setRotZ(bone.getRotZ() + zMod * (Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F));
        bone.setRotX(bone.getRotX() + xMod * Mth.sin(ageInTicks * 0.067F) * 0.05F);
    }
}
