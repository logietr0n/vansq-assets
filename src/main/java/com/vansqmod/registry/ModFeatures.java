package com.vansqmod.registry;

import com.vansqmod.VansqMod;
import com.vansqmod.worldgen.feature.AirMultifaceGrowthFeature;
import com.vansqmod.worldgen.feature.LostCavesWellFeature;
import com.vansqmod.worldgen.feature.RhodoheartCrystalFeature;
import com.vansqmod.worldgen.feature.config.LostCavesWellFeatureConfig;
import com.vansqmod.worldgen.feature.config.RhodoheartCrystalFeatureConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.MultifaceGrowthConfiguration;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, VansqMod.MODID);

    public static final Supplier<Feature<RhodoheartCrystalFeatureConfig>> RHODOHEART_CRYSTAL =
            FEATURES.register("rhodoheart_crystal",
                    () -> new RhodoheartCrystalFeature(RhodoheartCrystalFeatureConfig.CODEC));

    public static final Supplier<Feature<LostCavesWellFeatureConfig>> LOST_CAVES_WELL =
            FEATURES.register("lost_caves_well_chamber",
                    () -> new LostCavesWellFeature(LostCavesWellFeatureConfig.CODEC));

    public static final Supplier<Feature<MultifaceGrowthConfiguration>> AIR_MULTIFACE_GROWTH =
            FEATURES.register("air_multiface_growth",
                    () -> new AirMultifaceGrowthFeature(MultifaceGrowthConfiguration.CODEC));
}
