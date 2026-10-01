package com.vansqmod.entity;

import com.vansqmod.VansqMod;
import com.vansqmod.config.BlockedEntityConfig;
import com.vansqmod.registry.ModBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Spider Overhaul rejects every variant spider below Y 63 inside
 * {@code SpawnPlacements.checkSpawnRules}, and registers ocean spiders as
 * {@code ON_GROUND}, so vanilla {@code NaturalSpawner} never places them in
 * the ocean or Frosted Caves. This spawner copies vanilla’s monster pass
 * (shared cap, 24–128 player range, three weighted rolls per chunk) but never
 * calls {@code checkSpawnRules}.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class SpiderOverhaulNaturalSpawner {

    private static final int CHUNK_RANGE = 8;
    private static final int MAGIC_NUMBER = 17 * 17;
    private static final int MIN_PLAYER_DISTANCE_SQ = 24 * 24;
    private static final double MAX_PLAYER_DISTANCE = 128.0D;
    private static final int CLUSTER = 6;
    private static final int PACK_RETRIES = 3;
    private static final int VANILLA_PACK_TRIES = 3;
    private static final int SPAWN_WEIGHT = 1;
    private static final int CAP_CACHE_TICKS = 20;

    private static long lastCapGameTime = Long.MIN_VALUE;
    private static int lastCapLevelId = Integer.MIN_VALUE;
    private static boolean lastBelowCap = true;

    private SpiderOverhaulNaturalSpawner() {
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !ModList.get().isLoaded("spider_overhaul")
                || level.getDifficulty() == Difficulty.PEACEFUL
                || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
                || level.players().isEmpty()) {
            return;
        }
        EntityType<?> oceanType = BuiltInRegistries.ENTITY_TYPE.getOptional(OceanSpiderSpawns.OCEAN_SPIDER_ID).orElse(null);
        EntityType<?> iceType = BuiltInRegistries.ENTITY_TYPE.getOptional(IceSpiderSpawns.ICE_SPIDER_ID).orElse(null);
        if (oceanType == null && iceType == null) {
            return;
        }
        Set<ChunkPosRef> chunks = chunksAroundPlayers(level);
        if (chunks.isEmpty() || !belowMonsterCap(level, chunks.size())) {
            return;
        }
        for (ChunkPosRef chunkPos : chunks) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
            if (chunk != null) {
                spawnForChunk(level, chunk, oceanType, iceType);
            }
        }
    }

    private static boolean belowMonsterCap(ServerLevel level, int spawnableChunks) {
        int levelId = System.identityHashCode(level);
        long gameTime = level.getGameTime();
        if (levelId == lastCapLevelId && gameTime - lastCapGameTime < CAP_CACHE_TICKS) {
            return lastBelowCap;
        }
        int cap = Math.max(1, MobCategory.MONSTER.getMaxInstancesPerChunk() * spawnableChunks / MAGIC_NUMBER);
        Set<Mob> counted = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            AABB box = player.getBoundingBox().inflate(MAX_PLAYER_DISTANCE);
            for (Mob mob : level.getEntitiesOfClass(Mob.class, box, SpiderOverhaulNaturalSpawner::countsTowardMonsterCap)) {
                counted.add(mob);
            }
        }
        lastCapLevelId = levelId;
        lastCapGameTime = gameTime;
        lastBelowCap = counted.size() < cap;
        return lastBelowCap;
    }

    private static boolean countsTowardMonsterCap(Mob mob) {
        return mob.getType().getCategory() == MobCategory.MONSTER
                && !mob.isPersistenceRequired()
                && !mob.requiresCustomPersistence();
    }

    private static Set<ChunkPosRef> chunksAroundPlayers(ServerLevel level) {
        Set<ChunkPosRef> chunks = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            int cx = player.chunkPosition().x;
            int cz = player.chunkPosition().z;
            for (int dx = -CHUNK_RANGE; dx <= CHUNK_RANGE; dx++) {
                for (int dz = -CHUNK_RANGE; dz <= CHUNK_RANGE; dz++) {
                    if (dx * dx + dz * dz > CHUNK_RANGE * CHUNK_RANGE) {
                        continue;
                    }
                    chunks.add(new ChunkPosRef(cx + dx, cz + dz));
                }
            }
        }
        return chunks;
    }

    private static void spawnForChunk(
            ServerLevel level,
            LevelChunk chunk,
            EntityType<?> oceanType,
            EntityType<?> iceType
    ) {
        RandomSource random = level.random;
        int x = chunk.getPos().getMinBlockX() + random.nextInt(16);
        int z = chunk.getPos().getMinBlockZ() + random.nextInt(16);
        if (oceanType != null) {
            BlockPos floor = oceanFloorPos(chunk, x, z);
            if (level.isPositionEntityTicking(floor)
                    && level.getWorldBorder().isWithinBounds(floor)
                    && level.getBiome(floor).is(ModBiomeTags.SPAWNS_OCEAN_SPIDER)) {
                tryWeightedPack(level, floor, oceanType, true, random);
                return;
            }
        }
        if (iceType == null) {
            return;
        }
        BlockPos origin = randomPosInChunk(level, chunk, x, z, random);
        if (!level.isPositionEntityTicking(origin)
                || chunk.getBlockState(origin).isRedstoneConductor(chunk, origin)
                || !level.getWorldBorder().isWithinBounds(origin)
                || !level.getBiome(origin).is(ModBiomeTags.SPAWNS_ICE_SPIDER)) {
            return;
        }
        tryWeightedPack(level, origin, iceType, false, random);
    }

    /**
     * Vanilla {@code spawnCategoryForPosition} makes three independent weighted
     * picks per chunk. Weight {@code 1} against the biome’s existing monster
     * list, then one pack of 1.
     */
    private static void tryWeightedPack(
            ServerLevel level,
            BlockPos origin,
            EntityType<?> type,
            boolean ocean,
            RandomSource random
    ) {
        int totalWeight = monsterSpawnWeight(level, origin);
        int bound = totalWeight + SPAWN_WEIGHT;
        if (bound <= 0) {
            return;
        }
        for (int pack = 0; pack < VANILLA_PACK_TRIES; pack++) {
            if (random.nextInt(bound) >= SPAWN_WEIGHT) {
                continue;
            }
            if (tryPack(level, origin, type, ocean, random)) {
                return;
            }
        }
    }

    private static int monsterSpawnWeight(ServerLevel level, BlockPos pos) {
        int total = 0;
        for (MobSpawnSettings.SpawnerData data : level.getBiome(pos).value().getMobSettings().getMobs(MobCategory.MONSTER).unwrap()) {
            total += data.getWeight().asInt();
        }
        return total;
    }

    private static BlockPos oceanFloorPos(LevelChunk chunk, int x, int z) {
        int y = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
        return new BlockPos(x, y, z);
    }

    private static BlockPos randomPosInChunk(ServerLevel level, LevelChunk chunk, int x, int z, RandomSource random) {
        int surface = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1;
        int minY = level.getMinBuildHeight();
        int y = surface <= minY ? minY : Mth.randomBetweenInclusive(random, minY, surface);
        return new BlockPos(x, y, z);
    }

    private static boolean tryPack(
            ServerLevel level,
            BlockPos origin,
            EntityType<?> type,
            boolean ocean,
            RandomSource random
    ) {
        for (int attempt = 0; attempt < PACK_RETRIES; attempt++) {
            int x = origin.getX() + random.nextInt(CLUSTER) - random.nextInt(CLUSTER);
            int z = origin.getZ() + random.nextInt(CLUSTER) - random.nextInt(CLUSTER);
            if (!level.hasChunk(x >> 4, z >> 4)) {
                continue;
            }
            BlockPos pos = ocean
                    ? new BlockPos(x, level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z), z)
                    : new BlockPos(x, origin.getY(), z);
            if (!canPlace(level, type, pos, ocean, random) || !farEnoughFromPlayers(level, pos)) {
                continue;
            }
            if (place(level, type, pos, random)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canPlace(
            ServerLevel level,
            EntityType<?> type,
            BlockPos pos,
            boolean ocean,
            RandomSource random
    ) {
        if (BlockedEntityConfig.isBlocked(type) || !level.getWorldBorder().isWithinBounds(pos)) {
            return false;
        }
        if (ocean) {
            @SuppressWarnings("unchecked")
            EntityType<Mob> oceanSpider = (EntityType<Mob>) type;
            return OceanSpiderSpawns.canSpawn(oceanSpider, level, MobSpawnType.NATURAL, pos, random);
        }
        @SuppressWarnings("unchecked")
        EntityType<Monster> iceSpider = (EntityType<Monster>) type;
        return IceSpiderSpawns.canSpawn(iceSpider, level, MobSpawnType.NATURAL, pos, random);
    }

    private static boolean farEnoughFromPlayers(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY();
        double z = pos.getZ() + 0.5D;
        Player player = level.getNearestPlayer(x, y, z, MAX_PLAYER_DISTANCE, EntitySelector.NO_SPECTATORS);
        if (player == null) {
            return false;
        }
        return player.distanceToSqr(x, y, z) >= MIN_PLAYER_DISTANCE_SQ;
    }

    private static boolean place(ServerLevel level, EntityType<?> type, BlockPos pos, RandomSource random) {
        Entity created = type.create(level);
        if (!(created instanceof Mob mob)) {
            return false;
        }
        mob.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
        if (!level.noCollision(mob) || !mob.checkSpawnObstruction(level)) {
            mob.discard();
            return false;
        }
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null);
        level.addFreshEntityWithPassengers(mob);
        return !mob.isRemoved();
    }

    private record ChunkPosRef(int x, int z) {
    }
}
