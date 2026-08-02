package com.vansqmod.registry;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModItemTags {

    /** Items that can sweep without Sweeping Edge and deal full attack damage on sweeps. */
    public static final TagKey<Item> SWEEPING = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "sweeping")
    );

    /**
     * Scythes: FD straw harvesting (via {@code farmersdelight:straw_harvesters}) and
     * Quark hoe-style area crop harvest, without knife/hoe item tags.
     */
    public static final TagKey<Item> SCYTHE = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "scythe")
    );

    private ModItemTags() {
    }
}
