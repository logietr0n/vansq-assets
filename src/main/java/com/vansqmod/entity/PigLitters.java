package com.vansqmod.entity;

import com.vansqmod.VansqMod;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Pig litters with Vanilla Backport parent-variant inheritance.
 * Matches Quark defaults: base 2–3, plus 0–2 per parent that ate a golden carrot
 * (so one golden-carrot parent yields 2–5). Quark's litter spawn stays disabled.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class PigLitters {

    /** Inclusive, matching Quark Pig Litters defaults. */
    private static final int MIN_LITTER_SIZE = 2;
    private static final int MAX_LITTER_SIZE = 3;
    private static final int MIN_GOLDEN_CARROT_BOOST = 0;
    private static final int MAX_GOLDEN_CARROT_BOOST = 2;

    /** Set by Quark when a pig is fed a golden carrot (module still handles eating). */
    private static final String QUARK_GOLDEN_CARROT_TAG = "quark:AteGoldenCarrot";
    /** Our own tag if Quark's eat hook did not run. */
    private static final String VANSQ_GOLDEN_CARROT_TAG = "vansqmod:AteGoldenCarrot";

    private PigLitters() {
    }

    /** Called when a pig consumes a golden carrot (breeding food). */
    public static void markAteGoldenCarrot(Entity entity) {
        if (entity instanceof Pig pig && !pig.level().isClientSide()) {
            pig.getPersistentData().putBoolean(VANSQ_GOLDEN_CARROT_TAG, true);
            pig.getPersistentData().putBoolean(QUARK_GOLDEN_CARROT_TAG, true);
        }
    }

    /** Quark parity: clear golden-carrot boost once the pig is no longer in love. */
    @SubscribeEvent
    public static void onPigTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Pig pig) || pig.level().isClientSide()) {
            return;
        }
        if (!pig.isInLove() && ateGoldenCarrot(pig)) {
            pig.getPersistentData().remove(VANSQ_GOLDEN_CARROT_TAG);
            pig.getPersistentData().remove(QUARK_GOLDEN_CARROT_TAG);
        }
    }

    @SubscribeEvent
    public static void onPigBreed(BabyEntitySpawnEvent event) {
        if (!(event.getChild() instanceof Pig child) || child.level().isClientSide()) {
            return;
        }
        if (!(event.getParentA() instanceof Pig parentA) || !(event.getParentB() instanceof Pig parentB)) {
            return;
        }
        if (!(child.level() instanceof ServerLevel level)) {
            return;
        }

        // Primary baby: 50/50 parent trait when parents differ (VB trySetOffspringVariant).
        VanillaBackportPigVariants.applyOffspringVariant(child, parentA, parentB);
        PigForcePersist.markBreedingChild(child);

        int litterSize = Mth.nextInt(level.random, MIN_LITTER_SIZE, MAX_LITTER_SIZE);
        litterSize += goldenCarrotBoost(parentA, level);
        litterSize += goldenCarrotBoost(parentB, level);
        if (litterSize <= 1) {
            return;
        }

        Player cause = event.getCausedByPlayer();
        for (int i = 1; i < litterSize; i++) {
            AgeableMob born = parentA.getBreedOffspring(level, parentB);
            if (!(born instanceof Pig extra)) {
                continue;
            }
            // Authoritative parent roll — do not leave biome/default on litter extras.
            VanillaBackportPigVariants.applyOffspringVariant(extra, parentA, parentB);
            PigForcePersist.markBreedingChild(extra);

            extra.setBaby(true);
            extra.moveTo(parentA.getX(), parentA.getY(), parentA.getZ(), 0.0F, 0.0F);

            if (cause instanceof ServerPlayer player) {
                player.awardStat(Stats.ANIMALS_BRED);
                CriteriaTriggers.BRED_ANIMALS.trigger(player, parentA, parentB, extra);
            }

            level.addFreshEntityWithPassengers(extra);
            VansqMod.LOGGER.debug(
                    "[PigLitters] spawned litter piglet uuid={} ({} of {})",
                    extra.getUUID(),
                    i + 1,
                    litterSize);
        }
    }

    private static int goldenCarrotBoost(Pig parent, ServerLevel level) {
        if (!ateGoldenCarrot(parent)) {
            return 0;
        }
        return Mth.nextInt(level.random, MIN_GOLDEN_CARROT_BOOST, MAX_GOLDEN_CARROT_BOOST);
    }

    private static boolean ateGoldenCarrot(Pig pig) {
        var data = pig.getPersistentData();
        return data.getBoolean(QUARK_GOLDEN_CARROT_TAG) || data.getBoolean(VANSQ_GOLDEN_CARROT_TAG);
    }
}
