package com.vansqmod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;

/**
 * When off lichen moss, walk to the nearest patch within follow range.
 */
public class MellowedSeekLichenMossGoal extends MoveToBlockGoal {

    private final Mellowed mellowed;

    public MellowedSeekLichenMossGoal(Mellowed mellowed) {
        super(
                mellowed,
                1.0D,
                Math.max(8, Mth.floor(mellowed.getAttributeValue(Attributes.FOLLOW_RANGE))),
                7
        );
        this.mellowed = mellowed;
    }

    @Override
    public boolean canUse() {
        return !MellowedLichenMoss.isOnLichenMoss(this.mellowed) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !MellowedLichenMoss.isOnLichenMoss(this.mellowed) && super.canContinueToUse();
    }

    @Override
    public void start() {
        this.mellowed.setSeekingLichenMoss(true);
        super.start();
    }

    @Override
    public void stop() {
        this.mellowed.setSeekingLichenMoss(false);
        super.stop();
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        return MellowedLichenMoss.isLichenMoss(level.getBlockState(pos));
    }
}
