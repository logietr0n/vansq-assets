package com.vansqmod.integration.backpacks;

import com.spydnel.backpacks.registry.BPItems;
import com.vansqmod.VansqMod;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import top.theillusivec4.curios.api.CuriosApi;

@EventBusSubscriber(modid = VansqMod.MODID)
public final class BackpackCuriosIntegration {

    private static final BackpackCurioItem BACKPACK_CURIO = new BackpackCurioItem();

    private BackpackCuriosIntegration() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(BackpackCuriosIntegration::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CuriosApi.registerCurio(BPItems.BACKPACK.get(), BACKPACK_CURIO));
    }

    /**
     * Safety net: if anything still writes a backpack into CHEST, move it to Curios {@code back}
     * immediately in the same call (no deferred tick — that caused a visible flash).
     * <p>
     * A {@code LivingEntity#setItemSlot} mixin is not used because Lithium's equipment-tracking
     * transforms that method and breaks HEAD injections.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot() != EquipmentSlot.CHEST || !BackpackEquipment.isBackpack(event.getTo())) {
            return;
        }

        LivingEntity entity = event.getEntity();
        ItemStack backpack = event.getTo().copy();

        if (BackpackEquipment.getEquippedBackpack(entity).isEmpty()) {
            BackpackEquipment.setEquippedBackpack(entity, backpack);
        } else if (!entity.level().isClientSide() && entity instanceof Player player) {
            if (!player.getInventory().add(backpack)) {
                player.drop(backpack, false);
            }
        }

        entity.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
    }

    @SubscribeEvent
    public static void onItemEntityPickup(ItemEntityPickupEvent.Pre event) {
        BackpackPickupHandler.onItemEntityPickup(event);
    }

    /** Moves a legacy chest-slot backpack onto Curios {@code back} after login. */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!BackpackEquipment.isBackpack(chest) || BackpackEquipment.hasBackpackEquipped(player)) {
            return;
        }
        BackpackEquipment.setEquippedBackpack(player, chest.copy());
        player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
    }
}
