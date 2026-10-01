package com.vansqmod.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.fml.ModList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spider Overhaul jockey riders. Vanilla always mounts a skeleton; we swap
 * (or remove) that passenger after {@link Spider#finalizeSpawn}.
 */
public final class SpiderJockeys {

    private static final ResourceLocation PARCHED =
            ResourceLocation.fromNamespaceAndPath("minecraft", "parched");
    private static final ResourceLocation MURK =
            ResourceLocation.fromNamespaceAndPath("variantsandventures", "murk");
    private static final ResourceLocation VERDANT =
            ResourceLocation.fromNamespaceAndPath("variantsandventures", "verdant");
    private static final ResourceLocation STRAY = ResourceLocation.withDefaultNamespace("stray");
    private static final ResourceLocation BOGGED = ResourceLocation.withDefaultNamespace("bogged");
    private static final ResourceLocation SKELETON = ResourceLocation.withDefaultNamespace("skeleton");

    /**
     * {@code null} value means strip the jockey. Unlisted spiders are unchanged.
     */
    private static final Map<String, ResourceLocation> RIDER_BY_SPIDER = riderMap();

    private SpiderJockeys() {
    }

    private static Map<String, ResourceLocation> riderMap() {
        Map<String, ResourceLocation> map = new HashMap<>();
        map.put("desert_spider", PARCHED);
        map.put("ice_spider", STRAY);
        map.put("jungle_spider", VERDANT);
        map.put("sculk_spider", SKELETON);
        map.put("mushroom_spider", null);
        map.put("cavern_spider", SKELETON);
        map.put("ocean_spider", MURK);
        map.put("taiga_spider", SKELETON);
        map.put("birch_spider", SKELETON);
        map.put("savanna_spider", SKELETON);
        map.put("swamp_spider", BOGGED);
        return Collections.unmodifiableMap(map);
    }

    public static void replaceRider(
            Spider spider,
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnType
    ) {
        ResourceLocation spiderId = BuiltInRegistries.ENTITY_TYPE.getKey(spider.getType());
        if (spiderId == null || !"spider_overhaul".equals(spiderId.getNamespace())) {
            return;
        }
        if (!RIDER_BY_SPIDER.containsKey(spiderId.getPath())) {
            return;
        }
        ResourceLocation riderId = RIDER_BY_SPIDER.get(spiderId.getPath());
        List<Entity> passengers = List.copyOf(spider.getPassengers());
        Entity vanillaJockey = null;
        for (Entity passenger : passengers) {
            if (passenger.getType() == EntityType.SKELETON) {
                vanillaJockey = passenger;
                break;
            }
        }
        if (riderId == null) {
            for (Entity passenger : passengers) {
                passenger.stopRiding();
                passenger.discard();
            }
            return;
        }
        if (vanillaJockey == null || SKELETON.equals(riderId)) {
            return;
        }
        EntityType<?> riderType = BuiltInRegistries.ENTITY_TYPE.getOptional(riderId).orElse(null);
        if (riderType == null || riderType == EntityType.SKELETON || !modProvides(riderId)) {
            return;
        }
        Entity created = riderType.create(spider.level());
        if (!(created instanceof Mob rider)) {
            return;
        }
        vanillaJockey.stopRiding();
        vanillaJockey.discard();
        rider.moveTo(spider.getX(), spider.getY(), spider.getZ(), spider.getYRot(), 0.0F);
        rider.finalizeSpawn(level, difficulty, spawnType, null);
        rider.startRiding(spider);
    }

    private static boolean modProvides(ResourceLocation id) {
        if ("variantsandventures".equals(id.getNamespace())) {
            return ModList.get().isLoaded("variantsandventures");
        }
        if (PARCHED.equals(id)) {
            return ModList.get().isLoaded("barched");
        }
        return true;
    }
}
