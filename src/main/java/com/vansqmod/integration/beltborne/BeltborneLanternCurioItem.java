package com.vansqmod.integration.beltborne;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nonnull;

/**
 * Belt-only Curios behavior for Beltborne lamp items. Right-click equip is
 * {@link com.vansqmod.integration.curios.CuriosHotbarEquip}, not Curios' full-stack copy.
 */
public final class BeltborneLanternCurioItem implements ICurioItem {

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return BeltborneLanternEquipment.canPlaceLanternInBelt(slotContext, stack);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        // Curios copies the entire held stack on use. Hotbar equip is handled in CuriosHotbarEquip.
        return false;
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
        return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.0f, 1.0f);
    }
}
