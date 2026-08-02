package com.vansqmod.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.DeferredSoundType;

import java.util.function.Supplier;

/**
 * Rhodoheart crystal block sounds. Uses Galosphere's Allurite sounds when that mod is loaded.
 */
public final class RhodoheartSoundTypes {

    private static SoundType block;
    private static SoundType cluster;

    private RhodoheartSoundTypes() {
    }

    public static SoundType block() {
        if (block == null) {
            block = ModList.get().isLoaded("galosphere")
                    ? galosphereSoundType("allurite", SoundType.AMETHYST)
                    : SoundType.AMETHYST;
        }
        return block;
    }

    public static SoundType cluster() {
        if (cluster == null) {
            cluster = ModList.get().isLoaded("galosphere")
                    ? galosphereSoundType("allurite_cluster", SoundType.AMETHYST_CLUSTER)
                    : SoundType.AMETHYST_CLUSTER;
        }
        return cluster;
    }

    private static SoundType galosphereSoundType(String blockName, SoundType fallback) {
        return new DeferredSoundType(
                1.0F,
                1.0F,
                galosphereSound(blockName, "break", fallback.getBreakSound()),
                galosphereSound(blockName, "step", fallback.getStepSound()),
                galosphereSound(blockName, "place", fallback.getPlaceSound()),
                galosphereSound(blockName, "hit", fallback.getHitSound()),
                galosphereSound(blockName, "fall", fallback.getFallSound()));
    }

    private static Supplier<SoundEvent> galosphereSound(String blockName, String action, SoundEvent fallback) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("galosphere", "block." + blockName + "." + action);
        return () -> BuiltInRegistries.SOUND_EVENT.getOptional(id).orElse(fallback);
    }

    public static SoundEvent alluriteHit() {
        return BuiltInRegistries.SOUND_EVENT.getOptional(
                        ResourceLocation.fromNamespaceAndPath("galosphere", "block.allurite.hit"))
                .orElse(SoundEvents.AMETHYST_BLOCK_HIT);
    }
}
