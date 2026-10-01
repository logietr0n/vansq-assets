package com.vansqmod.entity;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * Cavern spiders have no natural/spawner presence. Spawn eggs and commands still work.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class CavernSpiderSpawns {

    public static final ResourceLocation CAVERN_SPIDER_ID =
            ResourceLocation.fromNamespaceAndPath("spider_overhaul", "cavern_spider");

    private CavernSpiderSpawns() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!ModList.get().isLoaded("spider_overhaul")
                || event.isCanceled()
                || event.isSpawnCancelled()) {
            return;
        }
        if (!CAVERN_SPIDER_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()))) {
            return;
        }
        MobSpawnType spawnType = event.getSpawnType();
        if (spawnType == MobSpawnType.SPAWN_EGG
                || spawnType == MobSpawnType.COMMAND
                || spawnType == MobSpawnType.DISPENSER) {
            return;
        }
        event.setCanceled(true);
        event.setSpawnCancelled(true);
    }
}
