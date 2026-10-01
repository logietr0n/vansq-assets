package com.vansqmod.client;

import com.vansqmod.VansqMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * EnchantWithMob keys overlays by entity type only. Some mobs swap skins on
 * the same type, so pick the matching full-skin overlay from the renderer texture.
 */
public final class VariantEnchantedEyes {

    private VariantEnchantedEyes() {
    }

    public static ResourceLocation overlayFor(
            LivingEntity entity,
            @Nullable ResourceLocation skin,
            ResourceLocation fallback
    ) {
        if (skin == null) {
            return fallback;
        }
        ResourceLocation mapped = overlayForSkin(skin.getPath());
        return mapped != null ? mapped : fallback;
    }

    @Nullable
    private static ResourceLocation overlayForSkin(String path) {
        return switch (path) {
            case "textures/entity/ecto_slab/cracked.png" -> overlay("enchanted_ecto_slab_cracked_eyes.png");
            case "textures/entity/ecto_slab/petrified.png" -> overlay("enchanted_ecto_slab_petrified_eyes.png");
            case "textures/entity/guster_red.png" -> overlay("enchanted_guster_red_eyes.png");
            case "textures/entity/guster_soul.png" -> overlay("enchanted_guster_soul_eyes.png");
            case "textures/entity/guster_silly.png" -> overlay("enchanted_guster_silly_eyes.png");
            case "textures/entity/murmur_angry.png" -> overlay("enchanted_murmur_angry_eyes.png");
            case "textures/entity/rocky_roller_angry.png" -> overlay("enchanted_rocky_roller_angry_eyes.png");
            case "textures/entity/rocky_roller_rolling.png" -> overlay("enchanted_rocky_roller_rolling_eyes.png");
            case "textures/entity/skelewag_1.png" -> overlay("enchanted_skelewag_1_eyes.png");
            case "textures/entity/harpy/harpy_ruffled_torpor.png" -> overlay("enchanted_harpy_ruffled_torpor_eyes.png");
            default -> null;
        };
    }

    private static ResourceLocation overlay(String file) {
        return ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "textures/entity/enchant_eye/" + file);
    }
}
