package com.vansqmod.integration.backpacks;

import com.spydnel.backpacks.registry.BPSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nonnull;

/**
 * Curios behavior for {@code backpacks:backpack}: only the {@code back} slot, equip-from-use enabled.
 */
public final class BackpackCurioItem implements ICurioItem {

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return BackpackEquipment.BACK_SLOT.equals(slotContext.identifier());
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return canEquip(slotContext, stack);
    }

    @Override
    public void onEquipFromUse(SlotContext slotContext, ItemStack stack) {
        // Default ICurioItem delegates to a stub that plays the generic armor sound; play ours instead.
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
        SoundEvent sound = BPSounds.BACKPACK_EQUIP.value();
        return new ICurio.SoundInfo(sound, 1.0f, 1.1f);
    }
}
