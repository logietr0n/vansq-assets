package com.vansqmod.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * Tiny Takeover baby humanoid armor for GeckoLib zombies. Vanilla
 * {@code HumanoidArmorLayer} remaps {@code textures/models/armor/X_layer_1/2.png}
 * to {@code textures/models/armor/baby/X.png} only when the parent model is a
 * Tiny Takeover baby mesh; Geo entities never qualify, so Putrid / Bouldering
 * do the same swap here.
 */
public final class BabyZombieGeoArmor {

    private static final String ARMOR_PREFIX = "textures/models/armor/";
    private static final CubeDeformation INNER_DEFORM = new CubeDeformation(-0.1F, 0.5F, 0.3F);
    private static final CubeDeformation OUTER_DEFORM = new CubeDeformation(-0.1F, 0.3F, 0.3F);

    private static BabyZombieArmorModel inner;
    private static BabyZombieArmorModel outer;

    private BabyZombieGeoArmor() {
    }

    public static HumanoidModel<?> modelFor(boolean baby, EquipmentSlot slot, HumanoidModel<?> adult) {
        if (!baby) {
            return adult;
        }
        return slot == EquipmentSlot.LEGS ? inner() : outer();
    }

    public static ModelPart partFor(boolean baby, String bone, EquipmentSlot slot, HumanoidModel<?> model) {
        if (baby && model instanceof BabyZombieArmorModel babyModel) {
            return babyModel.partFor(bone, slot);
        }
        return switch (bone) {
            case "head" -> model.head;
            case "right_arm" -> model.rightArm;
            case "left_arm" -> model.leftArm;
            case "right_leg" -> model.rightLeg;
            case "left_leg" -> model.leftLeg;
            default -> model.body;
        };
    }

    /**
     * Same string rewrite as Tiny Takeover's {@code HumanoidArmorLayerNeoForgeMixin}:
     * {@code namespace:textures/models/armor/iron_layer_1.png} becomes
     * {@code namespace:textures/models/armor/baby/iron.png}. Leather overlay
     * ({@code leather_layer_1_overlay.png}) lands on {@code baby/leather_overlay.png}.
     */
    public static ResourceLocation remapLayerTexture(ResourceLocation texture) {
        String path = texture.getPath();
        if (!path.startsWith(ARMOR_PREFIX)) {
            return texture;
        }
        String rest = path.substring(ARMOR_PREFIX.length())
                .replace("_layer_1", "")
                .replace("_layer_2", "");
        return ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), ARMOR_PREFIX + "baby/" + rest);
    }

    private static BabyZombieArmorModel inner() {
        if (inner == null) {
            inner = new BabyZombieArmorModel(BabyZombieArmorModel.createArmorLayer(INNER_DEFORM).bakeRoot());
        }
        return inner;
    }

    private static BabyZombieArmorModel outer() {
        if (outer == null) {
            outer = new BabyZombieArmorModel(BabyZombieArmorModel.createArmorLayer(OUTER_DEFORM).bakeRoot());
        }
        return outer;
    }
}
