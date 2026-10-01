package com.vansqmod.compat.jade;

import baguchi.enchantwithmob.api.IEnchantCap;
import baguchi.enchantwithmob.capability.MobEnchantHandler;
import baguchi.enchantwithmob.mobenchant.MobEnchant;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

import com.vansqmod.VansqMod;

/**
 * Lists EnchantWithMob enchants on the Jade overlay, under name and health.
 * Enchanted mobs use an aqua name; enchant text is light gray.
 */
public enum MobEnchantJadeProvider implements IEntityComponentProvider {
    INSTANCE;

    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "mob_enchants");

    /** After Jade health ({@code -4500}) and armor ({@code -4499}). */
    private static final int PRIORITY = -4490;

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public int getDefaultPriority() {
        return PRIORITY;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        Entity entity = accessor.getEntity();
        if (!(entity instanceof IEnchantCap cap) || !cap.getEnchantCap().hasEnchant()) {
            return;
        }

        Component name = entity.getName();
        if (name != null) {
            tooltip.replace(JadeIds.CORE_OBJECT_NAME, name.copy().withStyle(ChatFormatting.AQUA));
        }

        MutableComponent line = Component.empty();
        boolean first = true;
        for (MobEnchantHandler handler : cap.getEnchantCap().getMobEnchants()) {
            Holder<MobEnchant> holder = handler.getMobEnchant();
            if (holder == null) {
                continue;
            }
            if (!first) {
                line.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
            }
            first = false;
            line.append(formatEnchant(holder, handler.getEnchantLevel()));
        }
        if (!first) {
            tooltip.add(line.withStyle(ChatFormatting.GRAY));
        }
    }

    private static Component formatEnchant(Holder<MobEnchant> holder, int level) {
        MobEnchant enchant = holder.value();
        MutableComponent name = Component.translatable(enchant.getDescriptionId());
        if (level != 1 || enchant.getMaxLevel() != 1) {
            name.append(CommonComponents.SPACE);
            name.append(Component.translatable("enchantment.level." + level));
        }
        return name.withStyle(ChatFormatting.GRAY);
    }
}
