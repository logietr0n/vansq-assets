package com.vansqmod.entity;

import com.vansqmod.VansqMod;
import com.vansqmod.config.BlockedEntityConfig;
import com.vansqmod.config.ObliteratorItemConfig;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

/**
 * Enforces {@link BlockedEntityConfig} on every join/spawn/conversion path, including mods that
 * replace an allowed mob with a blocked one during {@link FinalizeSpawnEvent} (e.g. Hominid Bellman).
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class BlockedEntityHandler {

    private BlockedEntityHandler() {
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        BlockedEntityConfig.load();
        ObliteratorItemConfig.load();
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        VansqModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (BlockedEntityConfig.isBlocked(event.getEntity())) {
            event.setSpawnCancelled(true);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!BlockedEntityConfig.isBlocked(entity)) {
            return;
        }
        event.setCanceled(true);
        if (!event.getLevel().isClientSide()) {
            entity.discard();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onConversion(LivingConversionEvent.Pre event) {
        if (BlockedEntityConfig.isBlocked(event.getOutcome())) {
            event.setCanceled(true);
        }
    }
}
