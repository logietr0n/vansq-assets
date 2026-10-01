package com.vansqmod.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Lootr converts any block in its {@code convert/*} tags. Restrict that to chests
 * and barrels so shulkers, pots, and archaeology blocks stay vanilla.
 */
public final class LootrChestBarrelOnly {
    private static final TagKey<Block> CONVERT_CHESTS = tag("convert/chests");
    private static final TagKey<Block> CONVERT_TRAPPED_CHESTS = tag("convert/trapped_chests");
    private static final TagKey<Block> CONVERT_BARRELS = tag("convert/barrels");

    private LootrChestBarrelOnly() {
    }

    public static boolean allowsConversion(BlockState state) {
        return state.is(CONVERT_CHESTS) || state.is(CONVERT_TRAPPED_CHESTS) || state.is(CONVERT_BARRELS);
    }

    private static TagKey<Block> tag(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("lootr", path));
    }
}
