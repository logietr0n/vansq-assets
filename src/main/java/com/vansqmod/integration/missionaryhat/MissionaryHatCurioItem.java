package com.vansqmod.integration.missionaryhat;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nonnull;

/**
 * Curios {@code head} behavior for the Missionary hat, including the nearby-tame tick the helmet slot uses.
 */
public final class MissionaryHatCurioItem implements ICurioItem {

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return MissionaryHatEquipment.HEAD_SLOT.equals(slotContext.identifier());
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return canEquip(slotContext, stack);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!MissionaryHatEquipment.HEAD_SLOT.equals(slotContext.identifier())) {
            return;
        }
        if (!(slotContext.entity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        AABB box = player.getBoundingBox().inflate(2.5D);
        for (TamableAnimal animal : player.level().getEntitiesOfClass(
                TamableAnimal.class,
                box,
                candidate -> candidate.isAlive() && !candidate.isTame()
        )) {
            animal.tame(player);
        }
    }

    @Nonnull
    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.0F, 1.0F);
    }
}
