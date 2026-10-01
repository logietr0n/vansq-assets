package com.vansqmod.compat;

import com.github.alexthe666.alexsmobs.client.render.OctopusColorRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityLeafcutterAnt;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Packed ARGB tint for Alex's Mobs leafcutter-ant held-leaf overlays.
 * The overlay textures are greyscale and expect a foliage colour multiplier.
 */
public final class LeafcutterAntLeafColors {

    private LeafcutterAntLeafColors() {
    }

    public static int packedArgb(EntityLeafcutterAnt ant) {
        return FastColor.ARGB32.opaque(resolveRgb(ant));
    }

    private static int resolveRgb(EntityLeafcutterAnt ant) {
        BlockState harvested = ant.getHarvestedState();
        BlockPos pos = ant.getHarvestedPos();
        Minecraft minecraft = Minecraft.getInstance();

        if (harvested != null) {
            Level level = ant.level();
            if (level != null && pos != null) {
                int biomeColor = minecraft.getBlockColors().getColor(harvested, level, pos, 0);
                if (biomeColor != -1) {
                    return biomeColor;
                }
            }
            int sampled = OctopusColorRegistry.getBlockColor(harvested);
            if (sampled != -1 && sampled != 0xFFFFFF) {
                return sampled;
            }
        }

        int itemColor = minecraft.getItemColors().getColor(new ItemStack(Items.JUNGLE_LEAVES), 0);
        if (itemColor != -1) {
            return itemColor;
        }
        return FoliageColor.getDefaultColor();
    }
}
