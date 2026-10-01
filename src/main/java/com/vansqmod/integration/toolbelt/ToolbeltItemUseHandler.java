package com.vansqmod.integration.toolbelt;

import com.vansqmod.VansqMod;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Toolbelt does not override {@code Item#use}, so vanilla would still try to
 * equip it to legs. Sky Set on a Curios belt also has to run here instead of a
 * mixin on {@code Item#use}: Connector cannot load vansqmod classes from that
 * vanilla method (crash on any item use, including maggots).
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class ToolbeltItemUseHandler {

    private ToolbeltItemUseHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.isCanceled()) {
            return;
        }
        if (!ModList.get().isLoaded("caverns_and_chasms") || !ModList.get().isLoaded("curios")) {
            return;
        }

        Player player = event.getEntity();
        ItemStack held = event.getItemStack();
        InteractionResultHolder<ItemStack> skySet = ToolbeltSkySetSupport.trySkySetUse(
                held.getItem(), player.level(), player, event.getHand());
        if (skySet != null) {
            event.setCanceled(true);
            event.setCancellationResult(skySet.getResult());
            return;
        }

        if (!ToolbeltEquipment.isToolbelt(held)) {
            return;
        }
        // Always consume the use so the toolbelt cannot go onto legs. Hotbar Curios
        // equip runs at HIGHEST and already canceled on success.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
