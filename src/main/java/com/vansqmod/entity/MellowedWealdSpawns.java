package com.vansqmod.entity;

import com.vansqmod.VansqMod;
import com.vansqmod.registry.ModEntityTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * Same conversion technique as Variants & Ventures ({@code OnEntitySpawn}):
 * natural / chunk / structure zombie and skeleton spawns in {@code #vansqmod:has_mellowed}
 * become a Mellowed, and the original spawn is cancelled. Chance is 1 so those
 * two remain full replacements. Direct Mellowed rolls are added separately
 * (weight 25, pack 1, any light) because Weald lighting blocks most skeleton
 * and zombie spawns.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class MellowedWealdSpawns {

    public static final TagKey<Biome> HAS_MELLOWED = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "has_mellowed"));

    private MellowedWealdSpawns() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (event.isCanceled() || event.isSpawnCancelled()) {
            return;
        }
        if (tryConvert(event, EntityType.ZOMBIE) || tryConvert(event, EntityType.SKELETON)) {
            event.setCanceled(true);
            event.setSpawnCancelled(true);
        }
    }

    /**
     * Mirrors Variants & Ventures {@code OnEntitySpawn.handleOnEntitySpawn}.
     *
     * @return true if the original spawn should be cancelled
     */
    private static boolean tryConvert(FinalizeSpawnEvent event, EntityType<?> typeToReplace) {
        Mob entity = event.getEntity();
        MobSpawnType spawnType = event.getSpawnType();
        if (spawnType != MobSpawnType.NATURAL
                && spawnType != MobSpawnType.CHUNK_GENERATION
                && spawnType != MobSpawnType.STRUCTURE) {
            return false;
        }
        if (entity.getType() != typeToReplace) {
            return false;
        }

        ServerLevelAccessor world = event.getLevel();
        if (!world.getBiome(entity.blockPosition()).is(HAS_MELLOWED)) {
            return false;
        }

        Mob replacement = ModEntityTypes.MELLOWED.get().create(world.getLevel());
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
        replacement.setCanPickUpLoot(false);

        replacement.finalizeSpawn(world, event.getDifficulty(), spawnType, event.getSpawnData());
        return world.addFreshEntity(replacement);
    }
}
