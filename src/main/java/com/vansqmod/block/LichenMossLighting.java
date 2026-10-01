package com.vansqmod.block;

import com.vansqmod.entity.Mellowed;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * Galosphere lichen moss and Glow Moss Carpet light when walked on unless the
 * entity is sneaking. Melloweds, dropped items, and XP orbs should never
 * trigger or keep that light.
 */
public final class LichenMossLighting {

    private LichenMossLighting() {
    }

    public static boolean canLightFrom(Entity entity) {
        return entity != null && ignitesFromPresence(entity) && !entity.isSteppingCarefully();
    }

    public static boolean keepsLit(Entity entity) {
        return entity != null && ignitesFromPresence(entity);
    }

    private static boolean ignitesFromPresence(Entity entity) {
        return !(entity instanceof Mellowed)
                && !(entity instanceof ItemEntity)
                && !(entity instanceof ExperienceOrb);
    }
}
