package com.vansqmod.entity;

import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * Wander paths stay on lichen moss. Seeking moss uses vanilla walkability.
 */
public class MellowedWalkNodeEvaluator extends WalkNodeEvaluator {

    @Override
    public PathType getPathType(PathfindingContext context, int x, int y, int z) {
        PathType type = super.getPathType(context, x, y, z);
        if (!(this.mob instanceof Mellowed mellowed) || mellowed.isSeekingLichenMoss()) {
            return type;
        }
        if (type != PathType.WALKABLE && type != PathType.OPEN && type != PathType.WALKABLE_DOOR) {
            return type;
        }
        if (!MellowedLichenMoss.isStandableNode(context, x, y, z)) {
            return PathType.BLOCKED;
        }
        return type;
    }
}
