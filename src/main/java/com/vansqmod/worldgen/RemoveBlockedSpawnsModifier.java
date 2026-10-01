package com.vansqmod.worldgen;

import com.mojang.serialization.MapCodec;
import com.vansqmod.config.BlockedEntityConfig;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

/**
 * Drops {@link BlockedEntityConfig} mobs from biome spawn lists so they do not consume
 * spawn-weight rolls before being cancelled.
 */
public final class RemoveBlockedSpawnsModifier implements BiomeModifier {

    public static final RemoveBlockedSpawnsModifier INSTANCE = new RemoveBlockedSpawnsModifier();
    public static final MapCodec<RemoveBlockedSpawnsModifier> CODEC = MapCodec.unit(INSTANCE);

    private RemoveBlockedSpawnsModifier() {
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.AFTER_EVERYTHING) {
            return;
        }
        var spawns = builder.getMobSpawnSettings();
        for (MobCategory category : MobCategory.values()) {
            spawns.getSpawner(category).removeIf(data -> BlockedEntityConfig.isBlocked(data.type));
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
