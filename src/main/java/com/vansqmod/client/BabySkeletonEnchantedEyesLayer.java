package com.vansqmod.client;

import com.vansqmod.VansqMod;
import com.vansqmod.entity.SkeletonBabies;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;

/**
 * Born in Chaos baby skeletons swap {@code Texture} (default vs alternative, plus
 * the controlled skin). EnchantWithMob only keys eyes by entity type, so pick the
 * overlay from {@code getTexture()}.
 */
public class BabySkeletonEnchantedEyesLayer<T extends LivingEntity & GeoAnimatable> extends GeoEnchantedEyesLayer<T> {

    public BabySkeletonEnchantedEyesLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    @Nullable
    protected ResourceLocation resolveTexture(T entity) {
        if (SkeletonBabies.usesGeo(entity)) {
            return SkeletonBabies.enchantEyes(entity);
        }
        ResourceLocation fallback = super.resolveTexture(entity);
        String textureName = textureName(entity);
        if (textureName == null || textureName.isEmpty()) {
            return fallback;
        }
        ResourceLocation overlay = overlayFor(textureName, BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
        return overlay != null ? overlay : fallback;
    }

    @Nullable
    private static String textureName(LivingEntity entity) {
        try {
            Object value = entity.getClass().getMethod("getTexture").invoke(entity);
            return value instanceof String name ? name : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    @Nullable
    private static ResourceLocation overlayFor(String textureName, ResourceLocation entityType) {
        boolean minion = entityType != null && "baby_skeleton_minion".equals(entityType.getPath());
        String file = switch (textureName) {
            case "baby_skeleton_controlled" -> "enchanted_controlled_baby_skeleton_eyes.png";
            case "baby_skeleton" -> minion
                    ? "enchanted_baby_skeleton_minion_eyes.png"
                    : "enchanted_baby_skeleton_eyes.png";
            case "baby_skeleton_alternative" -> minion
                    ? "enchanted_baby_skeleton_minion_alternative_eyes.png"
                    : "enchanted_baby_skeleton_alternative_eyes.png";
            default -> null;
        };
        return file == null
                ? null
                : ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "textures/entity/enchant_eye/" + file);
    }
}
