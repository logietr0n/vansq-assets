package com.vansqmod.entity;

import com.vansqmod.VansqMod;
import com.vansqmod.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.SpawnData;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

import javax.annotation.Nullable;

/**
 * Converts leftover vanilla {@link EntityType#ZOMBIE} spawns below y=0 after biome
 * replacements. Variants and Ventures fires its {@code EntitySpawnEvent} from a
 * default-priority {@link FinalizeSpawnEvent} listener (Thicket/Gelid/etc., plus
 * Putrid). Mellowed runs at {@link EventPriority#HIGH}. This runs at
 * {@link EventPriority#LOWEST} so only a still-vanilla zombie is replaced.
 *
 * <p>Zombie spawners below y=0 are remapped the same way, without touching husk /
 * drowned / villager / mod variant spawners.</p>
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class BoulderingZombieSpawns {

    private static final ResourceLocation VANILLA_ZOMBIE_ID =
            ResourceLocation.withDefaultNamespace("zombie");

    private BoulderingZombieSpawns() {
    }

    public static boolean isBelowY0(BlockPos pos) {
        return pos.getY() < 0;
    }

    public static boolean isVanillaZombieId(@Nullable String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        ResourceLocation key = ResourceLocation.tryParse(id);
        return VANILLA_ZOMBIE_ID.equals(key);
    }

    public static EntityType<?> remapSpawnerType(EntityType<?> type, BlockPos pos) {
        if (type == EntityType.ZOMBIE && isBelowY0(pos)) {
            return ModEntityTypes.BOULDERING_ZOMBIE.get();
        }
        return type;
    }

    public static void remapSpawner(BaseSpawner spawner, @Nullable Level level, RandomSource random, BlockPos pos) {
        if (!isBelowY0(pos) || level == null) {
            return;
        }
        SpawnData next = ((BaseSpawnerAccess) (Object) spawner).vansqmod$getNextSpawnData();
        if (next == null) {
            return;
        }
        CompoundTag tag = next.getEntityToSpawn();
        if (!isVanillaZombieId(tag.getString("id"))) {
            return;
        }
        spawner.setEntityId(ModEntityTypes.BOULDERING_ZOMBIE.get(), level, random, pos);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (event.isCanceled() || event.isSpawnCancelled()) {
            return;
        }
        if (!shouldReplaceSpawnType(event.getSpawnType())) {
            return;
        }
        Mob entity = event.getEntity();
        if (entity.getType() != EntityType.ZOMBIE) {
            return;
        }
        if (!isBelowY0(entity.blockPosition())) {
            return;
        }
        if (tryConvert(event)) {
            event.setCanceled(true);
            event.setSpawnCancelled(true);
        }
    }

    private static boolean shouldReplaceSpawnType(MobSpawnType spawnType) {
        return spawnType == MobSpawnType.NATURAL
                || spawnType == MobSpawnType.CHUNK_GENERATION
                || spawnType == MobSpawnType.STRUCTURE
                || spawnType == MobSpawnType.SPAWNER
                || spawnType == MobSpawnType.TRIAL_SPAWNER
                || spawnType == MobSpawnType.REINFORCEMENT;
    }

    private static boolean tryConvert(FinalizeSpawnEvent event) {
        Mob entity = event.getEntity();
        ServerLevelAccessor world = event.getLevel();
        Mob replacement = ModEntityTypes.BOULDERING_ZOMBIE.get().create(world.getLevel());
        if (replacement == null) {
            return false;
        }

        replacement.moveTo(
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                replacement.getRandom().nextFloat() * 360.0F,
                0.0F);
        replacement.copyPosition(entity);
        replacement.yBodyRotO = entity.yBodyRotO;
        replacement.yBodyRot = entity.yBodyRot;
        replacement.yHeadRotO = entity.yHeadRotO;
        replacement.yHeadRot = entity.yHeadRot;
        replacement.setBaby(entity.isBaby());
        replacement.setNoAi(entity.isNoAi());
        replacement.setInvulnerable(entity.isInvulnerable());
        if (entity.hasCustomName()) {
            replacement.setCustomName(entity.getCustomName());
            replacement.setCustomNameVisible(entity.isCustomNameVisible());
        }
        if (entity.isPersistenceRequired()) {
            replacement.setPersistenceRequired();
        }
        replacement.setCanPickUpLoot(entity.canPickUpLoot());

        replacement.finalizeSpawn(world, event.getDifficulty(), event.getSpawnType(), event.getSpawnData());
        return world.addFreshEntity(replacement);
    }

    /**
     * Accessor implemented by {@code BaseSpawnerBoulderingZombieMixin}.
     */
    public interface BaseSpawnerAccess {
        @Nullable
        SpawnData vansqmod$getNextSpawnData();
    }
}
