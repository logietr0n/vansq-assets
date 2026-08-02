package com.vansqmod.registry;

import com.vansqmod.VansqMod;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class ModArmorMaterials {

    public static final Holder<ArmorMaterial> ROSE_GOLD_ARMOR_MATERIAL = register(
            "rose_gold",
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 3);
                map.put(ArmorItem.Type.LEGGINGS, 6);
                map.put(ArmorItem.Type.CHESTPLATE, 8);
                map.put(ArmorItem.Type.HELMET, 3);
                map.put(ArmorItem.Type.BODY, 8);
            }),
            22,        // enchantability
            0.0f,      // toughness
            0.0f,      // knockback resistance
            () -> ModItems.ROSE_GOLD_INGOT.get()
    );

    private static Holder<ArmorMaterial> register(String name,
                                                  EnumMap<ArmorItem.Type, Integer> protection,
                                                  int enchantability,
                                                  float toughness,
                                                  float knockbackResistance,
                                                  Supplier<Item> repairItem) {

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, name);
        Holder<SoundEvent> equipSound = SoundEvents.ARMOR_EQUIP_GOLD;
        Supplier<Ingredient> ingredient = () -> Ingredient.of(repairItem.get());
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(id));

        return Registry.registerForHolder(
                BuiltInRegistries.ARMOR_MATERIAL,
                id,
                new ArmorMaterial(protection, enchantability, equipSound, ingredient, layers, toughness, knockbackResistance)
        );
    }
}