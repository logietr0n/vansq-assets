package com.vansqmod.client;

import com.vansqmod.VansqMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Tiny Takeover's drowned-baby body mesh ({@code BabyZombieModel#createBodyLayer}).
 * Gelid/Thicket overlays bake the 0.25-inflate variant as their outer clothes.
 */
public final class DrownedBabyModels {

    public static final ModelLayerLocation BODY = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "drowned_baby"), "main");
    public static final ModelLayerLocation OUTER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "drowned_baby"), "outer");

    private DrownedBabyModels() {
    }

    public static LayerDefinition createBodyLayer(CubeDeformation deformation) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(16, 16)
                        .addBox(-2.0F, -2.5F, -1.0F, 4.0F, 5.0F, 2.0F, deformation),
                PartPose.offset(0.0F, 17.5F, 0.0F)
        );

        root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(3, 3)
                        .addBox(-3.0F, -6.25F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(35, 3)
                        .addBox(-3.0F, -6.15F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, 15.25F, 0.0F)
        );
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                        .texOffs(36, 16)
                        .addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, deformation),
                PartPose.offset(-3.0F, 15.5F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                        .texOffs(28, 16)
                        .addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, deformation),
                PartPose.offset(3.0F, 15.5F, 0.0F)
        );
        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                        .texOffs(8, 16)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, deformation),
                PartPose.offset(-1.0F, 20.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, deformation),
                PartPose.offset(1.0F, 20.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, 64, 64);
    }
}
