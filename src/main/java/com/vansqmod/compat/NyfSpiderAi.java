package com.vansqmod.compat;

import com.nyfaria.awcapi.entity.IAdvancedClimber;
import com.nyfaria.nyfsspiders.common.ModTags;
import com.nyfaria.nyfsspiders.common.entity.goal.BetterLeapAtTargetGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;

import java.util.function.Predicate;

/**
 * Nyf's Spiders mixins {@link net.minecraft.world.entity.monster.Spider}. Born in Chaos baby
 * and mother spiders extend that class, but their GeckoLib renderer never receives Nyf's
 * living-renderer orientation pass, so they do not crawl like other spiders. Ocean spiders
 * keep {@code CrabPounceGoal} (not vanilla leap); climber travel is skipped while they are
 * pouncing so Nyf does not eat that attack.
 */
public final class NyfSpiderAi {

    private static final AttributeModifier FOLLOW_RANGE_INCREASE = new AttributeModifier(
            ResourceLocation.fromNamespaceAndPath("nyfsspiders", "spider_follow_range_increase"),
            8.0D,
            AttributeModifier.Operation.ADD_VALUE
    );

    private NyfSpiderAi() {
    }

    public static void applyFollowRangeBonus(Mob mob) {
        AttributeInstance followRange = mob.getAttribute(Attributes.FOLLOW_RANGE);
        if (followRange != null && !followRange.hasModifier(FOLLOW_RANGE_INCREASE.id())) {
            followRange.addPermanentModifier(FOLLOW_RANGE_INCREASE);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void addBetterLeap(Mob mob) {
        if (mob instanceof IAdvancedClimber) {
            mob.goalSelector.addGoal(3, new BetterLeapAtTargetGoal(mob, 0.4F));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void ensureBetterLeap(Mob mob) {
        if (!(mob instanceof IAdvancedClimber)) {
            return;
        }
        boolean hasBetterLeap = mob.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrapped -> wrapped.getGoal() instanceof BetterLeapAtTargetGoal);
        if (hasBetterLeap) {
            return;
        }
        mob.goalSelector.getAvailableGoals().removeIf(wrapped -> wrapped.getGoal() instanceof LeapAtTargetGoal);
        mob.goalSelector.addGoal(3, new BetterLeapAtTargetGoal(mob, 0.4F));
    }

    public static float blockSlipperiness(Mob mob, BlockPos pos) {
        BlockState state = mob.level().getBlockState(pos);
        float slip = state.getBlock().getFriction() * 0.91F;
        if (state.is(ModTags.NON_CLIMBABLE)) {
            slip = 1.0F - (1.0F - slip) * 0.25F;
        }
        return slip;
    }

    public static boolean canClimbOnBlock(BlockState state) {
        return !state.is(ModTags.NON_CLIMBABLE);
    }

    public static float pathingMalus(
            IAdvancedClimber climber,
            BlockGetter level,
            Mob mob,
            PathType pathType,
            BlockPos pos,
            Vec3i offset,
            Predicate<Direction> predicate
    ) {
        if (offset.getY() != 0) {
            boolean canClimb = false;
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (Direction direction : Direction.values()) {
                if (!predicate.test(direction)) {
                    continue;
                }
                cursor.set(
                        pos.getX() + direction.getStepX(),
                        pos.getY() + direction.getStepY(),
                        pos.getZ() + direction.getStepZ()
                );
                if (climber.canClimbOnBlock(level.getBlockState(cursor), cursor)) {
                    canClimb = true;
                    break;
                }
            }
            if (!canClimb) {
                return -1.0F;
            }
        }
        return mob.getPathfindingMalus(pathType);
    }
}
