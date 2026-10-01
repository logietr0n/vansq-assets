package com.vansqmod.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Tiny Takeover's {@code BabyZombieModel#createArmorLayer} mesh, kept as a
 * GeckoLib-facing {@link HumanoidModel} so Putrid / Bouldering babies can use
 * the combined 64×64 {@code textures/models/armor/baby/*.png} sheets.
 */
public class BabyZombieArmorModel extends HumanoidModel<LivingEntity> {

    private final ModelPart waist;
    private final ModelPart bodyBase;
    private final ModelPart rightLegBase;
    private final ModelPart leftLegBase;
    private final ModelPart rightFoot;
    private final ModelPart leftFoot;

    public BabyZombieArmorModel(ModelPart root) {
        super(root);
        this.waist = root.getChild("waist");
        this.bodyBase = this.body.getChild("body_base");
        this.rightLegBase = this.rightLeg.getChild("right_leg_base");
        this.leftLegBase = this.leftLeg.getChild("left_leg_base");
        this.leftFoot = this.rightLeg.getChild("left_foot");
        this.rightFoot = this.leftLeg.getChild("right_foot");
    }

    /**
     * GeckoLib skips a part whose own {@code cubes} list is empty, so this
     * returns the child that actually has the slot's cubes rather than the
     * empty parent {@code body}/{@code right_leg}/{@code left_leg}.
     */
    public ModelPart partFor(String bone, EquipmentSlot slot) {
        return switch (bone) {
            case "head" -> this.head;
            case "right_arm" -> this.rightArm;
            case "left_arm" -> this.leftArm;
            case "right_leg" -> slot == EquipmentSlot.FEET ? this.leftFoot : this.rightLegBase;
            case "left_leg" -> slot == EquipmentSlot.FEET ? this.rightFoot : this.leftLegBase;
            default -> slot == EquipmentSlot.LEGS ? this.waist : this.bodyBase;
        };
    }

    public static LayerDefinition createArmorLayer(CubeDeformation deformation) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.5F, -7.0F, -4.5F, 9.0F, 8.0F, 8.0F, deformation),
                PartPose.offset(0.0F, 15.0F, 0.0F)
        );
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 18.0F, 0.0F)
        );
        body.addOrReplaceChild(
                "body_base",
                CubeListBuilder.create()
                        .texOffs(0, 17)
                        .addBox(-3.0F, -3.0F, -1.5F, 6.0F, 5.0F, 3.0F, deformation),
                PartPose.ZERO
        );
        root.addOrReplaceChild(
                "waist",
                CubeListBuilder.create()
                        .texOffs(0, 36)
                        .addBox(-3.0F, -1.2F, -1.49F, 5.9F, 2.0F, 2.9F, deformation.extend(-0.1F)),
                PartPose.offset(0.0F, 19.0F, 0.0F)
        );

        root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                        .texOffs(30, 25)
                        .addBox(-1.0F, 0.0F, -1.53F, 2.0F, 5.0F, 3.0F, deformation),
                PartPose.offset(-3.5F, 15.5F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                        .texOffs(30, 17)
                        .addBox(-1.0F, 0.0F, -1.53F, 2.0F, 5.0F, 3.0F, deformation),
                PartPose.offset(3.5F, 15.5F, 0.0F)
        );

        PartDefinition rightLeg = root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create(),
                PartPose.offset(-1.5F, 20.0F, 0.5F)
        );
        rightLeg.addOrReplaceChild(
                "right_leg_base",
                CubeListBuilder.create()
                        .texOffs(18, 17)
                        .addBox(-1.0F, -0.2F, -2.0F, 3.0F, 4.0F, 3.0F, deformation.extend(-0.1F)),
                PartPose.ZERO
        );
        rightLeg.addOrReplaceChild(
                "left_foot",
                CubeListBuilder.create()
                        .texOffs(0, 29)
                        .mirror()
                        .addBox(-1.0F, 2.9F, -2.0F, 3.0F, 1.0F, 3.0F, deformation)
                        .mirror(false),
                PartPose.ZERO
        );

        PartDefinition leftLeg = root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create(),
                PartPose.offset(1.5F, 20.0F, 0.5F)
        );
        leftLeg.addOrReplaceChild(
                "left_leg_base",
                CubeListBuilder.create()
                        .texOffs(18, 24)
                        .addBox(-2.0F, -0.2F, -2.0F, 3.0F, 4.0F, 3.0F, deformation.extend(-0.1F)),
                PartPose.ZERO
        );
        leftLeg.addOrReplaceChild(
                "right_foot",
                CubeListBuilder.create()
                        .texOffs(0, 25)
                        .addBox(-2.0F, 2.9F, -2.0F, 3.0F, 1.0F, 3.0F, deformation),
                PartPose.ZERO
        );

        return LayerDefinition.create(mesh, 64, 64);
    }
}
