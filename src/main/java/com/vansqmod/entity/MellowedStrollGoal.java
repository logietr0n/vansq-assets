package com.vansqmod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Idle between walks about 5× as long as vanilla stroll, and only pick lichen moss.
 */
public class MellowedStrollGoal extends WaterAvoidingRandomStrollGoal {

    private static final int HORIZONTAL_RANGE = 10;
    private static final int VERTICAL_RANGE = 7;

    public MellowedStrollGoal(Mellowed mellowed, double speedModifier) {
        super(mellowed, speedModifier);
        this.setInterval(RandomStrollGoal.DEFAULT_INTERVAL * 5);
    }

    @Override
    protected Vec3 getPosition() {
        BlockPos origin = this.mob.blockPosition();
        List<BlockPos> spots = new ArrayList<>();
        var level = this.mob.level();
        for (int dx = -HORIZONTAL_RANGE; dx <= HORIZONTAL_RANGE; dx++) {
            for (int dz = -HORIZONTAL_RANGE; dz <= HORIZONTAL_RANGE; dz++) {
                for (int dy = -VERTICAL_RANGE; dy <= VERTICAL_RANGE; dy++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    BlockPos feet = origin.offset(dx, dy, dz);
                    if (MellowedLichenMoss.isStandableNode(level, feet.getX(), feet.getY(), feet.getZ())) {
                        spots.add(feet.immutable());
                    }
                }
            }
        }
        if (spots.isEmpty()) {
            return null;
        }
        BlockPos chosen = spots.get(this.mob.getRandom().nextInt(spots.size()));
        return Vec3.atBottomCenterOf(chosen);
    }
}
