package com.vansqmod.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.fml.ModList;

/**
 * Shared Spider Overhaul combat helpers. Ice/ocean spider attributes live in
 * {@link PackEntityAttributes}.
 */
public final class SpiderOverhaulCombat {

    public static final ResourceLocation ICE_SPIDER_ID =
            ResourceLocation.fromNamespaceAndPath("spider_overhaul", "ice_spider");
    public static final ResourceLocation CACTUS_SPINE_ID =
            ResourceLocation.fromNamespaceAndPath("spider_overhaul", "cactus_spine");
    public static final ResourceLocation GIANT_SQUID_ID =
            ResourceLocation.fromNamespaceAndPath("alexsmobs", "giant_squid");

    /** 10 seconds. Spider Overhaul melee icy is 60 ticks (3 seconds). */
    public static final int ICY_DURATION_TICKS = 200;

    private SpiderOverhaulCombat() {
    }

    public static boolean isIceSpider(Entity entity) {
        return entity != null && ICE_SPIDER_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }

    public static boolean isCactusSpine(Entity entity) {
        return entity instanceof AbstractArrow
                && CACTUS_SPINE_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }

    public static void addGiantSquidTarget(Mob crab) {
        if (!ModList.get().isLoaded("alexsmobs") || crab.level().isClientSide) {
            return;
        }
        crab.targetSelector.addGoal(
                3,
                new NearestAttackableTargetGoal<>(
                        crab,
                        LivingEntity.class,
                        10,
                        true,
                        false,
                        living -> GIANT_SQUID_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(living.getType()))
                )
        );
    }
}
