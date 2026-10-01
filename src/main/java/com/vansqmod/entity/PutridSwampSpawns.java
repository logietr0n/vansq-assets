package com.vansqmod.entity;

import com.faboslav.variantsandventures.common.entity.event.OnEntitySpawn;
import com.faboslav.variantsandventures.common.events.entity.EntitySpawnEvent;
import com.vansqmod.VansqMod;
import com.vansqmod.registry.ModEntityTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Swamp zombies become Putrid through Variants and Ventures'
 * {@link OnEntitySpawn#handleOnEntitySpawn}, the same replacement used for Thicket.
 */
public final class PutridSwampSpawns {

    public static final TagKey<Biome> HAS_PUTRID = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "has_putrid"));

    /** Same 100% chance V&amp;V uses when a variant fully replaces its vanilla mob. */
    private static final double SPAWN_CHANCE = 100.0D;
    private static final int MINIMUM_Y_LEVEL = -64;

    private PutridSwampSpawns() {
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        if (!ModList.get().isLoaded("variantsandventures")) {
            return;
        }
        event.enqueueWork(() -> EntitySpawnEvent.EVENT.addListener(PutridSwampSpawns::handleEntitySpawn));
    }

    private static boolean handleEntitySpawn(EntitySpawnEvent event) {
        return OnEntitySpawn.handleOnEntitySpawn(
                event,
                EntityType.ZOMBIE,
                ModEntityTypes.PUTRID.get(),
                true,
                SPAWN_CHANCE,
                MINIMUM_Y_LEVEL,
                HAS_PUTRID
        );
    }
}
