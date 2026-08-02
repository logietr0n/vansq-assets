package com.vansqmod.integration.toolbelt;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

@EventBusSubscriber(modid = com.vansqmod.VansqMod.MODID)
public final class ToolbeltCuriosIntegration {

    private static final ToolbeltCurioItem TOOLBELT_CURIO = new ToolbeltCurioItem();

    private ToolbeltCuriosIntegration() {
    }

    public static void init(IEventBus modBus) {
        if (!ModList.get().isLoaded("caverns_and_chasms") || !ModList.get().isLoaded("curios")) {
            return;
        }
        modBus.addListener(ToolbeltCuriosIntegration::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> BuiltInRegistries.ITEM.getOptional(ToolbeltEquipment.TOOLBELT_ITEM)
                .ifPresent(item -> CuriosApi.registerCurio(item, TOOLBELT_CURIO)));
    }

    /**
     * Re-applies reach when the belt stack is equipped, unequipped, or enchanted ({@link CurioChangeEvent.State}).
     */
    @SubscribeEvent
    public static void onCurioChange(CurioChangeEvent event) {
        if (!ModList.get().isLoaded("caverns_and_chasms")) {
            return;
        }
        if (!ToolbeltEquipment.BELT_SLOT.equals(event.getIdentifier())) {
            return;
        }
        if (!ToolbeltEquipment.isToolbelt(event.getFrom()) && !ToolbeltEquipment.isToolbelt(event.getTo())) {
            return;
        }
        ToolbeltAttributeHandler.refresh(event.getEntity(), event.getTo());
    }

    /** Moves a toolbelt placed in the legs slot onto Curios {@code belt}. */
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!ModList.get().isLoaded("caverns_and_chasms")) {
            return;
        }
        if (event.getSlot() != EquipmentSlot.LEGS || !ToolbeltEquipment.isToolbelt(event.getTo())) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }

        entity.level().getServer().execute(() -> {
            ItemStack legs = entity.getItemBySlot(EquipmentSlot.LEGS);
            if (!ToolbeltEquipment.isToolbelt(legs)) {
                return;
            }
            if (ToolbeltEquipment.getEquippedToolbelt(entity).isEmpty()) {
                ToolbeltEquipment.setEquippedToolbelt(entity, legs.copy());
            }
            entity.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
            ToolbeltAttributeHandler.refresh(entity);
        });
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ModList.get().isLoaded("caverns_and_chasms")) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        if (!ToolbeltEquipment.isToolbelt(legs) || ToolbeltEquipment.hasToolbeltEquipped(player)) {
            ToolbeltAttributeHandler.refresh(player);
            return;
        }
        ToolbeltEquipment.setEquippedToolbelt(player, legs.copy());
        player.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        ToolbeltAttributeHandler.refresh(player);
    }
}
