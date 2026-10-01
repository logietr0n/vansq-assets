package com.vansqmod.client;

import com.vansqmod.VansqMod;
import com.vansqmod.entity.Mellowed;
import com.vansqmod.entity.SkeletonBabies;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

/**
 * Geo rest pose is the BlockBench model. Look and per-mob head roll are added on
 * top of that instead of replacing the hunched head rotation.
 */
public class MellowedModel extends DefaultedEntityGeoModel<Mellowed> {

    public MellowedModel() {
        super(ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "mellowed"), false);
    }

    @Override
    public ResourceLocation getModelResource(Mellowed animatable) {
        return animatable.isBaby() ? SkeletonBabies.geoModel(animatable) : super.getModelResource(animatable);
    }

    @Override
    public ResourceLocation getTextureResource(Mellowed animatable) {
        return animatable.isBaby() ? SkeletonBabies.bodyTexture(animatable) : super.getTextureResource(animatable);
    }

    @Override
    public void setCustomAnimations(Mellowed mellowed, long instanceId, AnimationState<Mellowed> state) {
        super.setCustomAnimations(mellowed, instanceId, state);
        if (mellowed.hurtTime > 0) {
            freezeLimbBones();
        }
        getBone("head").ifPresent(head -> {
            EntityModelData data = state.getData(DataTickets.ENTITY_MODEL_DATA);
            float yaw = 0.0F;
            float pitch = 0.0F;
            if (data != null) {
                yaw = Mth.clamp(Mth.wrapDegrees(data.netHeadYaw()), -85.0F, 85.0F);
                pitch = Mth.clamp(Mth.wrapDegrees(data.headPitch()), -90.0F, 90.0F);
            }
            float restPitch = mellowed.isBaby() ? 15.0F * Mth.DEG_TO_RAD : 0.0F;
            head.setRotX(head.getRotX() - restPitch + pitch * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() + yaw * Mth.DEG_TO_RAD);
            head.setRotZ(head.getRotZ() + mellowed.headRollRadians());
        });
    }

    private void freezeLimbBones() {
        for (String name : new String[] {"right_arm", "left_arm", "right_leg", "left_leg"}) {
            getBone(name).ifPresent(bone -> {
                var rest = bone.getInitialSnapshot();
                bone.updateRotation(rest.getRotX(), rest.getRotY(), rest.getRotZ());
            });
        }
    }
}
