package com.vansqmod.entity;

import com.vansqmod.registry.ModBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/**
 * Frostbiter ({@code spider_overhaul:ice_spider}) uses vanilla monster ground
 * spawning. Natural attempts go through {@link SpiderOverhaulNaturalSpawner}
 * so Spider Overhaul’s Y&lt;63 {@code SpawnPlacements} reject never runs.
 */
public final class IceSpiderSpawns {

    public static final ResourceLocation ICE_SPIDER_ID =
            ResourceLocation.fromNamespaceAndPath("spider_overhaul", "ice_spider");

    private IceSpiderSpawns() {
    }

    private static boolean typeResolved;
    private static EntityType<?> cachedType;

    public static boolean isIceSpider(EntityType<?> type) {
        EntityType<?> ice = cachedType();
        return ice != null && type == ice;
    }

    private static EntityType<?> cachedType() {
        if (!typeResolved) {
            typeResolved = true;
            if (ModList.get().isLoaded("spider_overhaul")) {
                cachedType = BuiltInRegistries.ENTITY_TYPE.getOptional(ICE_SPIDER_ID).orElse(null);
            }
        }
        return cachedType;
    }

    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        if (!ModList.get().isLoaded("spider_overhaul")) {
            return;
        }
        BuiltInRegistries.ENTITY_TYPE.getOptional(ICE_SPIDER_ID).ifPresent(type -> {
            @SuppressWarnings("unchecked")
            EntityType<Monster> iceSpider = (EntityType<Monster>) type;
            event.register(
                    iceSpider,
                    SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    IceSpiderSpawns::canSpawn,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
        });
    }

    public static boolean canSpawn(
            EntityType<Monster> type,
            ServerLevelAccessor level,
            MobSpawnType reason,
            BlockPos pos,
            RandomSource random
    ) {
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) {
            return true;
        }
        if (!level.getBiome(pos).is(ModBiomeTags.SPAWNS_ICE_SPIDER)) {
            return false;
        }
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }
}
