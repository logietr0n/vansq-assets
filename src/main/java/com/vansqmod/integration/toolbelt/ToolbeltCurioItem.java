package com.vansqmod.integration.toolbelt;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nonnull;

/**
 * Curios behavior for {@code caverns_and_chasms:toolbelt}: belt slot only.
 * Attributes are synced every tick on client and server via {@link #curioTick}.
 */
public final class ToolbeltCurioItem implements ICurioItem {

    private static final ResourceLocation EQUIP_SOUND =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "item.armor.equip_toolbelt");

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!ToolbeltEquipment.BELT_SLOT.equals(slotContext.identifier()) || slotContext.entity() == null) {
            return;
        }
        ToolbeltAttributeHandler.syncEquipped(slotContext.entity(), stack);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (ToolbeltEquipment.BELT_SLOT.equals(slotContext.identifier()) && slotContext.entity() != null) {
            ToolbeltAttributeHandler.syncEquipped(slotContext.entity(), stack);
        }
        ICurioItem.super.onEquip(slotContext, prevStack, stack);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (ToolbeltEquipment.BELT_SLOT.equals(slotContext.identifier()) && slotContext.entity() != null) {
            ToolbeltAttributeHandler.clear(slotContext.entity());
        }
        ICurioItem.super.onUnequip(slotContext, newStack, stack);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return ToolbeltEquipment.BELT_SLOT.equals(slotContext.identifier());
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return canEquip(slotContext, stack);
    }

    @Nonnull
    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getOptional(EQUIP_SOUND).orElse(null);
        if (sound == null) {
            return ICurioItem.super.getEquipSound(slotContext, stack);
        }
        return new ICurio.SoundInfo(sound, 1.0f, 1.0f);
    }
}
