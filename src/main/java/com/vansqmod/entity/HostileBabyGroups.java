package com.vansqmod.entity;

import com.vansqmod.VansqMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * On Hard, every newly spawned baby hostile gets one extra sibling. Skeleton
 * pairs become three; a baby zombie becomes a pair. Animals and creature-category
 * mobs are left alone. Chunk loads do not spawn extras.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class HostileBabyGroups {

    private static final ThreadLocal<Boolean> EXTRA = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private HostileBabyGroups() {
    }

    public static boolean isSpawningExtra() {
        return EXTRA.get();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.loadedFromDisk() || event.isCanceled()) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level) || level.getDifficulty() != Difficulty.HARD) {
            return;
        }
        if (!(event.getEntity() instanceof Mob mob) || !isHostileBaby(mob)) {
            return;
        }
        if (EXTRA.get() || SkeletonBabies.isPairSpawn()) {
            return;
        }
        spawnExtra(mob);
    }

    public static boolean isHostileBaby(Mob mob) {
        if (mob.getType().getCategory() != MobCategory.MONSTER) {
            return false;
        }
        return mob.isBaby() || SkeletonBabies.isMarkedBaby(mob);
    }

    private static void spawnExtra(Mob source) {
        if (!(source.level() instanceof ServerLevel level) || !SkeletonBabies.canTouchChunks(source)) {
            return;
        }
        EXTRA.set(true);
        try {
            Entity created = source.getType().create(level);
            if (!(created instanceof Mob extra)) {
                if (created != null) {
                    created.discard();
                }
                return;
            }
            Vec3 pos = SkeletonBabies.placeNear(level, source, source.position());
            extra.moveTo(
                    pos.x,
                    pos.y,
                    pos.z,
                    source.getYRot() + (source.getRandom().nextFloat() - 0.5F) * 40.0F,
                    0.0F);
            if (SkeletonBabies.rollsNaturalBaby(extra)) {
                SkeletonBabies.markAsBaby(extra);
                SkeletonBabies.markBabyRollDone(extra);
            } else {
                extra.setBaby(true);
            }
            extra.finalizeSpawn(
                    level,
                    level.getCurrentDifficultyAt(extra.blockPosition()),
                    MobSpawnType.REINFORCEMENT,
                    null);
            if (SkeletonBabies.rollsNaturalBaby(extra)) {
                SkeletonBabies.markAsBaby(extra);
            } else {
                extra.setBaby(true);
            }
            if (source.isPersistenceRequired()) {
                extra.setPersistenceRequired();
            }
            if (!level.addFreshEntity(extra)) {
                extra.discard();
            }
        } finally {
            EXTRA.set(false);
        }
    }
}
