package com.vansqmod.entity;

import com.vansqmod.registry.ModBiomeTags;
import com.vansqmod.registry.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

/**
 * Spider crabs ({@code spider_overhaul:ocean_spider}) spawn on the ocean floor
 * (water or seagrass/kelp on solid ground). Natural attempts go through
 * {@link SpiderOverhaulNaturalSpawner} so Spider Overhaul’s Y&lt;63
 * {@code SpawnPlacements} reject never runs.
 */
public final class OceanSpiderSpawns {

    public static final ResourceLocation OCEAN_SPIDER_ID =
            ResourceLocation.fromNamespaceAndPath("spider_overhaul", "ocean_spider");

    private OceanSpiderSpawns() {
    }

    private static boolean typeResolved;
    private static EntityType<?> cachedType;

    public static boolean isOceanSpider(EntityType<?> type) {
        EntityType<?> ocean = cachedType();
        return ocean != null && type == ocean;
    }

    private static EntityType<?> cachedType() {
        if (!typeResolved) {
            typeResolved = true;
            if (ModList.get().isLoaded("spider_overhaul")) {
                cachedType = BuiltInRegistries.ENTITY_TYPE.getOptional(OCEAN_SPIDER_ID).orElse(null);
            }
        }
        return cachedType;
    }

    /**
     * Must be registered on the mod bus. {@link net.neoforged.fml.common.EventBusSubscriber}
     * defaults to the game bus, so this is hooked from {@link com.vansqmod.VansqMod}.
     */
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        if (!ModList.get().isLoaded("spider_overhaul")) {
            return;
        }
        BuiltInRegistries.ENTITY_TYPE.getOptional(OCEAN_SPIDER_ID).ifPresent(type -> {
            @SuppressWarnings("unchecked")
            EntityType<Mob> oceanSpider = (EntityType<Mob>) type;
            event.register(
                    oceanSpider,
                    SpawnPlacementTypes.IN_WATER,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    OceanSpiderSpawns::canSpawn,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE
            );
        });
    }

    public static boolean canSpawn(
            EntityType<Mob> type,
            ServerLevelAccessor level,
            MobSpawnType reason,
            BlockPos pos,
            RandomSource random
    ) {
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) {
            return true;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (!level.getBiome(pos).is(ModBiomeTags.SPAWNS_OCEAN_SPIDER)) {
            return false;
        }
        if (!isWaterOrOceanVegetation(level, pos)) {
            return false;
        }
        return level.getBlockState(pos.below()).blocksMotion();
    }

    public static boolean isWaterOrOceanVegetation(BlockGetter level, BlockPos pos) {
        if (level.getFluidState(pos).is(FluidTags.WATER)) {
            return true;
        }
        return level.getBlockState(pos).is(ModBlockTags.OCEAN_VEGETATION);
    }
}
