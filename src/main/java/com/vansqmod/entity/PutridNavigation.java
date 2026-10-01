package com.vansqmod.entity;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Ground navigation that treats water as walkable, matching Charm of Sinking
 * land-like movement. Vanilla {@link GroundPathNavigation} rejects
 * {@link PathType#WATER} nodes, so zombies path around ponds even when the
 * water malus is 0.
 */
public class PutridNavigation extends GroundPathNavigation {

    public PutridNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected boolean hasValidPathType(PathType pathType) {
        return pathType == PathType.WATER || super.hasValidPathType(pathType);
    }
}
