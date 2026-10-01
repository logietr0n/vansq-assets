package com.vansqmod.integration.backpacks;

import com.spydnel.backpacks.registry.BPItems;
import com.spydnel.backpacks.registry.BPSounds;
import com.vansqmod.VansqMod;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Objects;

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
        try {
            handleFilledBackpackPickup(event);
        } catch (Throwable t) {
            VansqMod.LOGGER.error("Failed to handle backpack item pickup", t);
        }
    }

    /**
     * Filled backpacks on the ground equip into Curios {@code back} instead of the inventory.
     * Kept in this class so player ticks never lazy-load a second handler class.
     */
    private static void handleFilledBackpackPickup(ItemEntityPickupEvent.Pre event) {
        ItemEntity itemEntity = event.getItemEntity();
        ItemStack itemStack = itemEntity.getItem();
        boolean hasContainer = itemStack.has(DataComponents.CONTAINER);
        boolean isEmpty = Objects.equals(itemStack.get(DataComponents.CONTAINER), ItemContainerContents.EMPTY);

        if (itemStack.is(BPItems.BACKPACK) && hasContainer && !isEmpty) {
            Player player = event.getPlayer();
            if (BackpackEquipment.getEquippedBackpack(player).isEmpty()
                    && !itemEntity.hasPickUpDelay()) {
                BackpackEquipment.setEquippedBackpack(player, itemStack);
                player.level().playSound(
                        null,
                        player.blockPosition(),
                        BPSounds.BACKPACK_EQUIP.value(),
                        SoundSource.PLAYERS,
                        1.0F,
                        1.1F
                );
                player.take(itemEntity, 1);
                itemEntity.discard();
                player.awardStat(Stats.ITEM_PICKED_UP.get(itemStack.getItem()), 1);
                player.onItemPickup(itemEntity);
            }
            event.setCanPickup(TriState.FALSE);
        }
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
