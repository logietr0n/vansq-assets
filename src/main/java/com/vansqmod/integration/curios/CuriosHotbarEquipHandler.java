package com.vansqmod.integration.curios;

import com.vansqmod.VansqMod;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Right-click equips one item into a Curios slot (belt, spyglass, atlas, …), swapping
 * when the slot is occupied, and cancels vanilla use so lantern stacks are not swallowed
 * and atlas/spyglass do not crash.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class CuriosHotbarEquipHandler {

    private CuriosHotbarEquipHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!ModList.get().isLoaded("curios") || event.isCanceled()) {
            return;
        }
        Player player = event.getEntity();
        boolean equipped = CuriosHotbarEquip.tryEquipOne(player, event.getHand());
        if (!equipped && !CuriosHotbarEquip.shouldSuppressVanillaUse(event.getItemStack())) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide()));
    }

    /**
     * Looking at a block uses {@code RightClickBlock} first. Supplementaries then asks
     * Moonlight for atlas map data and crashes; swallow that path the same way as air-click.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!ModList.get().isLoaded("curios") || event.isCanceled()) {
            return;
        }
        if (!CuriosHotbarEquip.shouldSuppressVanillaUse(event.getItemStack())) {
            return;
        }
        Player player = event.getEntity();
        CuriosHotbarEquip.tryEquipOne(player, event.getHand());
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide()));
        event.setUseBlock(TriState.FALSE);
        event.setUseItem(TriState.FALSE);
    }
}
