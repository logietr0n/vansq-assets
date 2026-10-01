package com.vansqmod.entity;

import com.vansqmod.VansqMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * Replaces Born in Chaos {@code baby_skeleton} / {@code bone_imp} with baby
 * {@code skeleton} / {@code wither_skeleton}. Minions and controlled babies are
 * left alone.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class SkeletonBabyMerge {

    private SkeletonBabyMerge() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (event.isCanceled() || event.isSpawnCancelled() || event.getLevel().isClientSide()) {
            return;
        }
        if (!ModList.get().isLoaded("born_in_chaos_v1")) {
            return;
        }
        Mob entity = event.getEntity();
        if (!SkeletonBabies.isMergeSource(entity)) {
            return;
        }
        if (tryConvert(event.getLevel().getLevel(), entity)) {
            event.setCanceled(true);
            event.setSpawnCancelled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.isCanceled()) {
            return;
        }
        if (!ModList.get().isLoaded("born_in_chaos_v1")) {
            return;
        }
        Entity entity = event.getEntity();
        if (!SkeletonBabies.isMergeSource(entity) || !(entity instanceof Mob mob)) {
            return;
        }
        if (tryConvert((ServerLevel) event.getLevel(), mob)) {
            event.setCanceled(true);
            mob.discard();
        }
    }

    private static boolean tryConvert(ServerLevel level, Mob source) {
        if (source.isRemoved()) {
            return false;
        }
        EntityType<?> replacementType = SkeletonBabies.replacementType(source.getType());
        Mob replacement = (Mob) replacementType.create(level);
        if (replacement == null) {
            return false;
        }
        replacement.copyPosition(source);
        replacement.setYRot(source.getYRot());
        replacement.setXRot(source.getXRot());
        replacement.yBodyRot = source.yBodyRot;
        replacement.yHeadRot = source.yHeadRot;
        replacement.setNoAi(source.isNoAi());
        replacement.setInvulnerable(source.isInvulnerable());
        if (source.hasCustomName()) {
            replacement.setCustomName(source.getCustomName());
            replacement.setCustomNameVisible(source.isCustomNameVisible());
        }
        if (source.isPersistenceRequired()) {
            replacement.setPersistenceRequired();
        }
        String texture = SkeletonBabies.bicTextureName(source);
        if (texture != null && !texture.isEmpty()) {
            SkeletonBabies.setTextureKey(replacement, texture);
        }
        SkeletonBabies.markAsBaby(replacement);
        boolean added = level.addFreshEntity(replacement);
        if (added) {
            source.discard();
        }
        return added;
    }
}
