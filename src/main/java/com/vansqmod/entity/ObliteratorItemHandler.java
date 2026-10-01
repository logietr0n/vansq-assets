package com.vansqmod.entity;

import com.vansqmod.VansqMod;
import com.vansqmod.config.ObliteratorItemConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

/**
 * Enforces {@link ObliteratorItemConfig} on loot-adjacent gameplay: mob gear, item entities,
 * pickups, inventories, and drop lists. Loot table rolls are filtered separately.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class ObliteratorItemHandler {

    private ObliteratorItemHandler() {
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        ObliteratorItemConfig.load();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        stripEquipment(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            if (ObliteratorItemConfig.isBlocked(itemEntity.getItem())) {
                event.setCanceled(true);
                itemEntity.discard();
            }
            return;
        }
        if (event.getEntity() instanceof LivingEntity living && !(living instanceof Player)) {
            stripEquipment(living);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || entity instanceof Player) {
            return;
        }
        if (ObliteratorItemConfig.isBlocked(event.getTo())) {
            entity.setItemSlot(event.getSlot(), ItemStack.EMPTY);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPickup(ItemEntityPickupEvent.Pre event) {
        if (ObliteratorItemConfig.isBlocked(event.getItemEntity().getItem())) {
            event.setCanPickup(TriState.FALSE);
            event.getItemEntity().discard();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        stripInventory(event.getEntity().getInventory());
        stripEquipment(event.getEntity());
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        AbstractContainerMenu menu = event.getContainer();
        for (Slot slot : menu.slots) {
            if (ObliteratorItemConfig.isBlocked(slot.getItem())) {
                slot.set(ItemStack.EMPTY);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        event.getDrops().removeIf(drop -> ObliteratorItemConfig.isBlocked(drop.getItem()));
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        event.getDrops().removeIf(drop -> ObliteratorItemConfig.isBlocked(drop.getItem()));
    }

    public static void stripEquipment(LivingEntity entity) {
        if (entity == null) {
            return;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (ObliteratorItemConfig.isBlocked(stack)) {
                entity.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    private static void stripInventory(Inventory inventory) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (ObliteratorItemConfig.isBlocked(inventory.getItem(i))) {
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
