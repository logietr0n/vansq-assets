package com.vansqmod.registry;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModBlockTags {

    /** Surfaces Melloweds wander and naturally spawn on. */
    public static final TagKey<Block> MELLOWED_PATHING = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "mellowed_pathing")
    );

    /** Seagrass and kelp that count as a valid spider-crab spawn cell alongside water. */
    public static final TagKey<Block> OCEAN_VEGETATION = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "ocean_vegetation")
    );

    private ModBlockTags() {
    }
}
