package com.vansqmod.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Same fast chew as Born in Chaos fried maggot ({@code getUseDuration} 22).
 */
public class GoldenMaggotItem extends Item {

    public GoldenMaggotItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 22;
    }
}
