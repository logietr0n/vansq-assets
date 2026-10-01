package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * Pack mob stats previously applied through AttributeSetter datapacks.
 * {@code setBaseValue} on join is used because {@link net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent}
 * cannot change attributes the entity already has.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class PackEntityAttributes {

    private PackEntityAttributes() {
    }

    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(living.getType());
        switch (id.toString()) {
            case "galosphere:preserved" -> {
                setBase(living, Attributes.MAX_HEALTH, 16.0D);
                setBase(living, Attributes.MOVEMENT_SPEED, 0.35D);
            }
            case "variantsandventures:verdant" -> {
                setBase(living, Attributes.MAX_HEALTH, 12.0D);
                setBase(living, Attributes.MOVEMENT_SPEED, 0.3D);
                setBase(living, Attributes.SCALE, 0.9D);
                setBase(living, Attributes.JUMP_STRENGTH, 0.42D);
                setBase(living, Attributes.SAFE_FALL_DISTANCE, 6.0D);
            }
            case "minecraft:enderman" -> addValue(living, Attributes.ATTACK_KNOCKBACK, 1.5D, "enderman_knockback");
            case "hominid:juggernaut" -> setBase(living, Attributes.ARMOR, 10.0D);
            case "minecraft:piglin_brute" -> setBase(living, Attributes.ARMOR, 10.0D);
            case "spider_overhaul:ocean_spider" -> {
                setBase(living, Attributes.FOLLOW_RANGE, 40.0D);
                setBase(living, Attributes.ATTACK_DAMAGE, 2.0D);
                setBase(living, Attributes.MOVEMENT_SPEED, 0.3D);
            }
            case "spider_overhaul:ice_spider" -> setBase(living, Attributes.ATTACK_KNOCKBACK, 0.5D);
            default -> {
            }
        }
    }

    private static void setBase(LivingEntity living, Holder<Attribute> attribute, double value) {
        AttributeInstance instance = living.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        stripAttributeSetter(instance);
        instance.setBaseValue(value);
        if (attribute.is(Attributes.MAX_HEALTH) && living.getHealth() > living.getMaxHealth()) {
            living.setHealth(living.getMaxHealth());
        }
    }

    private static void addValue(LivingEntity living, Holder<Attribute> attribute, double amount, String key) {
        AttributeInstance instance = living.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        stripAttributeSetter(instance);
        instance.addOrReplacePermanentModifier(new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, key),
                amount,
                AttributeModifier.Operation.ADD_VALUE
        ));
    }

    private static void stripAttributeSetter(AttributeInstance instance) {
        instance.getModifiers().stream()
                .map(AttributeModifier::id)
                .filter(id -> "attributesetter".equals(id.getNamespace()))
                .toList()
                .forEach(instance::removeModifier);
    }
}
