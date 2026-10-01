package com.vansqmod.integration.missionaryhat;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import top.theillusivec4.curios.api.CuriosApi;

@EventBusSubscriber(modid = VansqMod.MODID)
public final class MissionaryHatCuriosIntegration {

    private static final MissionaryHatCurioItem HAT_CURIO = new MissionaryHatCurioItem();

    private MissionaryHatCuriosIntegration() {
    }

    public static void init(IEventBus modBus) {
        if (!ModList.get().isLoaded("born_in_chaos_v1") || !ModList.get().isLoaded("curios")) {
            return;
        }
        modBus.addListener(MissionaryHatCuriosIntegration::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> BuiltInRegistries.ITEM.getOptional(MissionaryHatEquipment.HAT_ITEM)
                .ifPresent(item -> CuriosApi.registerCurio(item, HAT_CURIO)));
    }

    /**
     * Safety net: if anything still writes the hat into HEAD, move it to Curios {@code head}
     * immediately in the same call (no deferred tick — that caused a visible flash on backpacks).
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot() != EquipmentSlot.HEAD || !MissionaryHatEquipment.isHat(event.getTo())) {
            return;
        }

        LivingEntity entity = event.getEntity();
        ItemStack hat = event.getTo().copy();

        if (!MissionaryHatEquipment.tryEquipHat(entity, hat)
                && !entity.level().isClientSide()
                && entity instanceof Player player) {
            if (!player.getInventory().add(hat)) {
                player.drop(hat, false);
            }
        }

        entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
    }

    /** Moves a legacy helmet-slot hat onto Curios {@code head} after login. */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!MissionaryHatEquipment.isHat(helmet)) {
            return;
        }
        ItemStack hat = helmet.copy();
        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        if (!MissionaryHatEquipment.tryEquipHat(player, hat) && !player.getInventory().add(hat)) {
            player.drop(hat, false);
        }
    }
}
