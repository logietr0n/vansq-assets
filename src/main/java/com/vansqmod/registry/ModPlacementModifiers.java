package com.vansqmod.registry;

import com.vansqmod.VansqMod;
import com.vansqmod.worldgen.placement.SurfaceBiomeFilter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModPlacementModifiers {

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, VansqMod.MODID);

    public static final Supplier<PlacementModifierType<SurfaceBiomeFilter>> SURFACE_BIOME =
            PLACEMENT_MODIFIER_TYPES.register("surface_biome", () -> () -> SurfaceBiomeFilter.CODEC);

    private ModPlacementModifiers() {
    }
}
