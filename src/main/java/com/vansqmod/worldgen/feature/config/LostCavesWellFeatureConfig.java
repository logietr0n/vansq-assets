package com.vansqmod.worldgen.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record LostCavesWellFeatureConfig(
        int spacingChunks,
        int separationChunks,
        int spacingYSections,
        int separationYSections,
        int minY,
        int maxY
) implements FeatureConfiguration {

    public static final Codec<LostCavesWellFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("spacing_chunks").forGetter(LostCavesWellFeatureConfig::spacingChunks),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("separation_chunks").forGetter(LostCavesWellFeatureConfig::separationChunks),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("spacing_y_sections", 2).forGetter(LostCavesWellFeatureConfig::spacingYSections),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("separation_y_sections", 1).forGetter(LostCavesWellFeatureConfig::separationYSections),
            Codec.INT.fieldOf("min_y").forGetter(LostCavesWellFeatureConfig::minY),
            Codec.INT.fieldOf("max_y").forGetter(LostCavesWellFeatureConfig::maxY)
    ).apply(instance, LostCavesWellFeatureConfig::new));
}
