package com.vansqmod.entity;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;

/**
 * Born in Chaos baby skeletons and bone imps are melee mobs. Skeleton babies keep that
 * behavior instead of the adult bow, sun-flee, and stroll goals.
 */
public final class SkeletonBabyAi {

    private static final double MELEE_SPEED = 1.2D;
    private static final double STROLL_SPEED = 0.8D;
    private static final double MOVE_SPEED = 0.31D;

    private static final Set<AbstractSkeleton> CONFIGURED =
            Collections.newSetFromMap(new WeakHashMap<>());

    private SkeletonBabyAi() {
    }

    public static void apply(AbstractSkeleton skeleton) {
        if (skeleton.level().isClientSide()
                || !SkeletonBabies.isMarkedBaby(skeleton)
                || !SkeletonBabies.canTouchChunks(skeleton)) {
            return;
        }
        skeleton.goalSelector.getAvailableGoals().removeIf(wrapped -> {
            var goal = wrapped.getGoal();
            if (goal instanceof SkeletonBabyMeleeGoal) {
                return false;
            }
            if (goal instanceof MeleeAttackGoal
                    || goal instanceof RestrictSunGoal
                    || goal instanceof FleeSunGoal
                    || goal instanceof WaterAvoidingRandomStrollGoal) {
                return true;
            }
            String name = goal.getClass().getName();
            return name.contains("RangedBow") || name.contains("RangedCrossbow") || name.contains("BowAttack");
        });
        boolean hasMelee = skeleton.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrapped -> wrapped.getGoal() instanceof SkeletonBabyMeleeGoal);
        if (!hasMelee) {
            skeleton.goalSelector.addGoal(2, new SkeletonBabyMeleeGoal(skeleton));
        }
        boolean hasStroll = skeleton.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrapped -> wrapped.getGoal() instanceof RandomStrollGoal
                        && !(wrapped.getGoal() instanceof WaterAvoidingRandomStrollGoal));
        if (!hasStroll) {
            skeleton.goalSelector.addGoal(5, new RandomStrollGoal(skeleton, STROLL_SPEED));
        }
        if (!CONFIGURED.add(skeleton)) {
            return;
        }
        var speed = skeleton.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(MOVE_SPEED);
        }
        if (skeleton instanceof WitherSkeleton) {
            skeleton.goalSelector.getAvailableGoals().removeIf(wrapped -> wrapped.getGoal() instanceof AvoidEntityGoal<?>);
        } else {
            avoid(skeleton, "net.mcreator.borninchaosv.entity.DreadHoundEntity");
            avoid(skeleton, "net.mcreator.borninchaosv.entity.DireHoundLeaderEntity");
        }
        skeleton.targetSelector.getAvailableGoals().removeIf(wrapped ->
                wrapped.getGoal() instanceof NearestAttackableTargetGoal<?>
                        || wrapped.getGoal() instanceof HurtByTargetGoal);
        skeleton.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(skeleton, Player.class, false));
        skeleton.targetSelector.addGoal(3, new HurtByTargetGoal(skeleton).setAlertOthers());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void avoid(AbstractSkeleton skeleton, String className) {
        try {
            Class<? extends LivingEntity> type = (Class<? extends LivingEntity>) Class.forName(className);
            skeleton.goalSelector.addGoal(5, new AvoidEntityGoal(skeleton, type, 6.0F, 1.0D, 1.0D));
        } catch (ClassNotFoundException ignored) {
        }
    }

    private static final class SkeletonBabyMeleeGoal extends MeleeAttackGoal {
        private SkeletonBabyMeleeGoal(AbstractSkeleton skeleton) {
            super(skeleton, MELEE_SPEED, false);
        }
    }
}
