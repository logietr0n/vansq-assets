package com.vansqmod.item;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.vansqmod.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Head-slot curio for the golden and silver crowns.
 * Experience boost and magic protection use Quark's attribute icons. Fortune, looting,
 * and magic-damage conversion are described in {@code vansq_tooltips.json} instead.
 */
public final class CrownCurioItem implements ICurioItem {

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return CrownBonuses.HEAD_SLOT.equals(slotContext.identifier());
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return canEquip(slotContext, stack);
    }

    @Nonnull
    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        var sound = stack.is(ModItems.SILVER_CROWN.get())
                ? SoundEvents.ARMOR_EQUIP_IRON.value()
                : SoundEvents.ARMOR_EQUIP_GOLD.value();
        return new ICurio.SoundInfo(sound, 1.0F, 1.0F);
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext,
            ResourceLocation id,
            ItemStack stack
    ) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = ArrayListMultimap.create();
        if (CrownBonuses.HEAD_SLOT.equals(slotContext.identifier())) {
            CrownBonuses.addWornModifiers(modifiers, stack);
        }
        return modifiers;
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, Item.TooltipContext context, ItemStack stack) {
        return tooltips;
    }

    @Override
    @SuppressWarnings("removal")
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        return tooltips;
    }

    @Override
    public int getFortuneLevel(SlotContext slotContext, LootContext lootContext, ItemStack stack) {
        if (!stack.is(ModItems.GOLDEN_CROWN.get())) {
            return 0;
        }
        return (int) CrownBonuses.FORTUNE + enchantmentLevel(lootContext, Enchantments.FORTUNE, stack);
    }

    @Override
    public int getLootingLevel(SlotContext slotContext, LootContext lootContext, ItemStack stack) {
        if (!stack.is(ModItems.GOLDEN_CROWN.get())) {
            return 0;
        }
        return (int) CrownBonuses.LOOTING + enchantmentLevel(lootContext, Enchantments.LOOTING, stack);
    }

    private static int enchantmentLevel(LootContext lootContext, ResourceKey<Enchantment> enchantment, ItemStack stack) {
        if (lootContext == null) {
            return 0;
        }
        return lootContext.getLevel().registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .flatMap(lookup -> lookup.get(enchantment))
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
                .orElse(0);
    }
}
