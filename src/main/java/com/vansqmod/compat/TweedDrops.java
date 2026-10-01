package com.vansqmod.compat;

import com.vansqmod.debug.VansqDebugState;
import com.vansqmod.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Farmer's Delight straw harvest can resolve to tweed instead (1/8196).
 */
public final class TweedDrops {

    public static final int CHANCE = 8196;
    private static final ResourceLocation STRAW_ID =
            ResourceLocation.fromNamespaceAndPath("farmersdelight", "straw");

    private TweedDrops() {
    }

    public static boolean isStraw(Item item) {
        return item != null && STRAW_ID.equals(BuiltInRegistries.ITEM.getKey(item));
    }

    public static boolean isStraw(ItemStack stack) {
        return !stack.isEmpty() && isStraw(stack.getItem());
    }

    public static boolean shouldReplace(RandomSource random) {
        return VansqDebugState.rareEventSucceeds(random, CHANCE);
    }

    public static ItemStack maybeReplace(ItemStack stack, RandomSource random) {
        if (!isStraw(stack) || !shouldReplace(random)) {
            return stack;
        }
        return new ItemStack(ModItems.TWEED.get(), stack.getCount());
    }

    public static void replaceInList(List<ItemStack> stacks, RandomSource random) {
        if (stacks == null) {
            return;
        }
        for (int i = 0; i < stacks.size(); i++) {
            stacks.set(i, maybeReplace(stacks.get(i), random));
        }
    }
}
