package com.vansqmod.integration.toolbelt;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * C&amp;C enchantments on the toolbelt are defined for {@code EquipmentSlotGroup.LEGS}; when the belt is on Curios
 * {@code belt}, attribute effects (Extending) must be applied manually.
 */
public final class ToolbeltEnchantmentSupport {

    private static final ResourceKey<Enchantment> EXTENDING_KEY = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "extending"));

    private ToolbeltEnchantmentSupport() {
    }

    public static int getExtendingLevel(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        ItemEnchantments enchants = stack.get(DataComponents.ENCHANTMENTS);
        if (enchants == null || enchants.isEmpty()) {
            return 0;
        }
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchants.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            if (matchesExtending(holder)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }

    private static boolean matchesExtending(Holder<Enchantment> holder) {
        if (holder.is(EXTENDING_KEY)) {
            return true;
        }
        return holder.unwrapKey().map(EXTENDING_KEY::equals).orElse(false);
    }

    /**
     * Matches {@code data/caverns_and_chasms/enchantment/extending.json} linear amount (1.0 per level).
     */
    public static double extendingRangeBonus(int level) {
        return level <= 0 ? 0.0D : (double) level;
    }
}
