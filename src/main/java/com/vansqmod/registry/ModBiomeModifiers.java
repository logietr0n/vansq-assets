package com.vansqmod.registry;

import com.mojang.serialization.MapCodec;
import com.vansqmod.VansqMod;
import com.vansqmod.worldgen.RemoveBlockedSpawnsModifier;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModBiomeModifiers {

    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, VansqMod.MODID);

    public static final Supplier<MapCodec<RemoveBlockedSpawnsModifier>> REMOVE_BLOCKED_SPAWNS =
            SERIALIZERS.register("remove_blocked_spawns", () -> RemoveBlockedSpawnsModifier.CODEC);

    private ModBiomeModifiers() {
    }
}
