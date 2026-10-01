package com.vansqmod.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Variants & Ventures ships Gelid/Thicket baby sheets but the renderers always
 * return the adult paths. Point babies at those V&amp;V textures.
 */
public final class VvZombieVariantSkins {

    private VvZombieVariantSkins() {
    }

    public static ResourceLocation body(Entity entity, ResourceLocation fallback) {
        String variant = babyVariant(entity);
        if (variant == null) {
            return fallback;
        }
        return file(variant, variant + "_baby");
    }

    public static ResourceLocation overlay(Entity entity, ResourceLocation fallback) {
        String variant = babyVariant(entity);
        if (variant == null) {
            return fallback;
        }
        return file(variant, variant + "_baby_overlay");
    }

    private static String babyVariant(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !living.isBaby()) {
            return null;
        }
        ResourceLocation type = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (!"variantsandventures".equals(type.getNamespace())) {
            return null;
        }
        return switch (type.getPath()) {
            case "gelid", "thicket" -> type.getPath();
            default -> null;
        };
    }

    private static ResourceLocation file(String folder, String name) {
        return ResourceLocation.fromNamespaceAndPath(
                "variantsandventures",
                "textures/entity/" + folder + "/" + name + ".png"
        );
    }
}
