package com.vansqmod.client;

import com.vansqmod.entity.SkeletonBabies;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Bogged;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class SkeletonBabyGeoModel<T extends LivingEntity & GeoAnimatable> extends GeoModel<T> {

    @Override
    public ResourceLocation getModelResource(T entity) {
        return SkeletonBabies.geoModel(entity);
    }

    @Override
    public ResourceLocation getTextureResource(T entity) {
        return SkeletonBabies.bodyTexture(entity);
    }

    @Override
    public ResourceLocation getAnimationResource(T entity) {
        return SkeletonBabies.geoAnimation(entity);
    }

    @Override
    public void setCustomAnimations(T entity, long instanceId, AnimationState<T> state) {
        super.setCustomAnimations(entity, instanceId, state);
        if (entity instanceof Bogged bogged) {
            getBone("shroom").ifPresent(bone -> bone.setHidden(bogged.isSheared()));
        }
    }
}
