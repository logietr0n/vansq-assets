package com.vansqmod.entity;

import com.vansqmod.VansqMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;

/**
 * Baby zombies (and other {@link EntityTypeTags#ZOMBIES} babies) have half max health.
 * Uses a transient multiplied-total modifier so extra HP from gear/effects is halved too,
 * and growing up / converting restores the adult max.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class BabyZombieHealth {

    public static final ResourceLocation MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "baby_half_health");

    private static final AttributeModifier HALF_HEALTH = new AttributeModifier(
            MODIFIER_ID,
            -0.5D,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );

    private BabyZombieHealth() {
    }

    public static void apply(LivingEntity entity) {
        if (!isZombieFamily(entity)) {
            return;
        }
        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }
        maxHealth.removeModifier(MODIFIER_ID);
        if (entity.isBaby()) {
            maxHealth.addTransientModifier(HALF_HEALTH);
            if (entity.getHealth() > entity.getMaxHealth()) {
                entity.setHealth(entity.getMaxHealth());
            }
        }
    }

    public static boolean isZombieFamily(LivingEntity entity) {
        return entity instanceof Zombie || entity.getType().is(EntityTypeTags.ZOMBIES);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity living) {
            apply(living);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onConverted(LivingConversionEvent.Post event) {
        apply(event.getOutcome());
    }
}
