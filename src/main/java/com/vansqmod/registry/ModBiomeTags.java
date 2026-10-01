package com.vansqmod.registry;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class ModBiomeTags {

    public static final TagKey<Biome> SPAWNS_OCEAN_SPIDER = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "spawns_ocean_spider")
    );

    public static final TagKey<Biome> SPAWNS_ICE_SPIDER = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "spawns_ice_spider")
    );

    /** Warm / desert / badlands: zombie gold tools. */
    public static final TagKey<Biome> ZOMBIE_GOLD_TOOLS = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "zombie_gold_tools")
    );

    /** Cold / snowy / icy: zombie silver tools. */
    public static final TagKey<Biome> ZOMBIE_SILVER_TOOLS = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "zombie_silver_tools")
    );

    private ModBiomeTags() {
    }
}
