package com.vansqmod.integration.tetherpotion;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
public final class TetherPotionCuriosIntegration {

    private static final TetherPotionCurioItem POTION_CURIO = new TetherPotionCurioItem();

    private TetherPotionCuriosIntegration() {
    }

    public static void init(IEventBus modBus) {
        if (!ModList.get().isLoaded(TetherPotionEquipment.CNC_MODID) || !ModList.get().isLoaded("curios")) {
            return;
        }
        modBus.addListener(TetherPotionCuriosIntegration::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (Item item : BuiltInRegistries.ITEM) {
                if (TetherPotionEquipment.isPotionItem(item)) {
                    CuriosApi.registerCurio(item, POTION_CURIO);
                }
            }
        });
    }

    /**
     * Safety net: if anything still writes a wearable potion into HEAD, move it to Curios {@code head}
     * immediately in the same call (no deferred tick — that caused a visible flash on backpacks).
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot() != EquipmentSlot.HEAD || !TetherPotionEquipment.isPotion(event.getTo())) {
            return;
        }

        LivingEntity entity = event.getEntity();
        ItemStack potion = event.getTo().copy();

        if (!TetherPotionEquipment.tryEquipPotion(entity, potion)
                && !entity.level().isClientSide()
                && entity instanceof Player player) {
            if (!player.getInventory().add(potion)) {
                player.drop(potion, false);
            }
        }

        entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
    }

    /** Moves a legacy helmet-slot potion onto Curios {@code head} after login. */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!TetherPotionEquipment.isPotion(helmet)) {
            return;
        }
        ItemStack potion = helmet.copy();
        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        if (!TetherPotionEquipment.tryEquipPotion(player, potion) && !player.getInventory().add(potion)) {
            player.drop(potion, false);
        }
    }
}
