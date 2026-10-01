package com.vansqmod.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import tallestred.piglinproliferation.common.items.BucklerItem;

/**
 * Movement is a single look-direction impulse. Dash state (pose, bash, and
 * damage block) still lasts the configured charge ticks. Distances are 7
 * blocks for a Buckler and 4 for a Doubuckler on typical ground friction.
 */
public final class BucklerDashes {

    private static final double BUCKLER_BLOCKS = 7.0D;
    private static final double DOUBUCKLER_BLOCKS = 4.0D;
    /**
     * Dirt friction 0.6 * 0.91 air = 0.546, so coast distance is
     * {@code v0 / (1 - 0.546)}.
     */
    private static final double GROUND_FRICTION = 0.546D;

    private BucklerDashes() {
    }

    public static void impulse(LivingEntity entity, ItemStack stack) {
        if (entity == null || stack.isEmpty() || !(stack.getItem() instanceof BucklerItem)) {
            return;
        }
        double distance = stack.getItem() instanceof RoseGoldDoubucklerItem
                ? DOUBUCKLER_BLOCKS
                : BUCKLER_BLOCKS;
        double v0 = distance * (1.0D - GROUND_FRICTION);
        Vec3 look = entity.getViewVector(1.0F);
        double lx = look.x;
        double lz = look.z;
        double len = Math.hypot(lx, lz);
        if (len < 1.0E-4D) {
            return;
        }
        lx /= len;
        lz /= len;
        Vec3 current = entity.getDeltaMovement();
        entity.setDeltaMovement(current.x + lx * v0, current.y, current.z + lz * v0);
        entity.hasImpulse = true;
        BucklerItem.CHARGE_SPEED_BOOST.removeModifier(entity);
    }
}
