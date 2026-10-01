package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = VansqMod.MODID)
public final class DoubucklerHandsEvents {

    private DoubucklerHandsEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!ModList.get().isLoaded("piglinproliferation")) {
            return;
        }
        DoubucklerHands.tick(event.getEntity());
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!ModList.get().isLoaded("piglinproliferation")) {
            return;
        }
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player
                && event.getSlot() == net.minecraft.world.entity.EquipmentSlot.OFFHAND) {
            DoubucklerHands.ejectFromOffhand(player);
        }
    }

    @SubscribeEvent
    public static void onStackedOnOffhand(ItemStackedOnOtherEvent event) {
        if (!ModList.get().isLoaded("piglinproliferation")) {
            return;
        }
        Slot slot = event.getSlot();
        if (!(slot.container instanceof Inventory) || slot.getContainerSlot() != Inventory.SLOT_OFFHAND) {
            return;
        }
        if (DoubucklerHands.isDoubuckler(event.getCarriedItem())
                || DoubucklerHands.isDoubuckler(event.getStackedOnItem())) {
            event.setCanceled(true);
        }
    }
}
