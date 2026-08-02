package com.vansqmod.worldgen.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record RhodoheartCrystalFeatureConfig(
        float crystal_chance,
        float glimmering_cluster_chance,
        float small_bud_chance,
        float medium_bud_chance,
        float large_bud_chance
) implements FeatureConfiguration {

    public static final Codec<RhodoheartCrystalFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("crystal_chance").forGetter(RhodoheartCrystalFeatureConfig::crystal_chance),
            Codec.floatRange(0.0F, 1.0F).fieldOf("glimmering_cluster_chance").forGetter(RhodoheartCrystalFeatureConfig::glimmering_cluster_chance),
            Codec.floatRange(0.0F, 1.0F).fieldOf("small_bud_chance").forGetter(RhodoheartCrystalFeatureConfig::small_bud_chance),
            Codec.floatRange(0.0F, 1.0F).fieldOf("medium_bud_chance").forGetter(RhodoheartCrystalFeatureConfig::medium_bud_chance),
            Codec.floatRange(0.0F, 1.0F).fieldOf("large_bud_chance").forGetter(RhodoheartCrystalFeatureConfig::large_bud_chance)
    ).apply(instance, RhodoheartCrystalFeatureConfig::new));
}
