package com.vansqmod.compat;

import com.vansqmod.entity.IceSnowballProjectile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

/**
 * Pack item component overrides previously applied through AttributeSetter
 * ({@code durability} / {@code max_stack}).
 */
public final class PackItemComponents {

    private PackItemComponents() {
    }

    public static void onModifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        setMaxDamage(event, "minecraft:copper_sword", 190);
        setMaxDamage(event, "minecraft:copper_pickaxe", 190);
        setMaxDamage(event, "minecraft:copper_axe", 190);
        setMaxDamage(event, "minecraft:copper_shovel", 190);
        setMaxDamage(event, "minecraft:copper_hoe", 190);
        setMaxDamage(event, "minecraft:copper_helmet", 165);
        setMaxDamage(event, "minecraft:copper_chestplate", 240);
        setMaxDamage(event, "minecraft:copper_leggings", 225);
        setMaxDamage(event, "minecraft:copper_boots", 195);

        Item iceSnowball = BuiltInRegistries.ITEM.get(IceSnowballProjectile.ITEM_ID);
        if (iceSnowball != Items.AIR) {
            event.modify(iceSnowball, builder -> builder.set(DataComponents.MAX_STACK_SIZE, 64));
        }
    }

    private static void setMaxDamage(ModifyDefaultComponentsEvent event, String id, int maxDamage) {
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
        if (item == Items.AIR) {
            return;
        }
        event.modify(item, builder -> builder.set(DataComponents.MAX_DAMAGE, maxDamage));
    }
}
