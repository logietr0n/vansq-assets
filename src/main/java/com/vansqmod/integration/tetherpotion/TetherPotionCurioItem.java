package com.vansqmod.integration.tetherpotion;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nonnull;

/**
 * Curios {@code head} behavior for Caverns &amp; Chasms wearable potions.
 */
public final class TetherPotionCurioItem implements ICurioItem {

    private static final ResourceLocation EQUIP_SOUND =
            ResourceLocation.fromNamespaceAndPath(TetherPotionEquipment.CNC_MODID, "item.tether_potion.equip");

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return TetherPotionEquipment.HEAD_SLOT.equals(slotContext.identifier());
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return canEquip(slotContext, stack);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity != null && !entity.level().isClientSide() && TetherPotionEquipment.isTetherPotion(stack)) {
            TetherPotionEquipment.updateTetherPotionEffects(entity, stack, true);
        }
        ICurioItem.super.onEquip(slotContext, prevStack, stack);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity != null && !entity.level().isClientSide() && TetherPotionEquipment.isTetherPotion(stack)) {
            TetherPotionEquipment.updateTetherPotionEffects(entity, stack, false);
            TetherPotionEquipment.setTetherCooldown(stack, 600);
        }
        ICurioItem.super.onUnequip(slotContext, newStack, stack);
    }

    @Override
    public void onEquipFromUse(SlotContext slotContext, ItemStack stack) {
        ICurio.SoundInfo sound = getEquipSound(slotContext, stack);
        LivingEntity entity = slotContext.entity();
        entity.level().playSound(
                null,
                entity.blockPosition(),
                sound.soundEvent(),
                SoundSource.PLAYERS,
                sound.volume(),
                sound.pitch()
        );
    }

    @Nonnull
    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getOptional(EQUIP_SOUND)
                .orElse(SoundEvents.BOTTLE_FILL);
        return new ICurio.SoundInfo(sound, 1.0F, 1.0F);
    }
}
