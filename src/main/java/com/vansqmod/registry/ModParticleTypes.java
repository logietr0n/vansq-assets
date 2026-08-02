package com.vansqmod.registry;

import com.vansqmod.VansqMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticleTypes {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, VansqMod.MODID);

    public static final Supplier<SimpleParticleType> RHODOHEART_RAIN =
            PARTICLE_TYPES.register("rhodoheart_rain", () -> new SimpleParticleType(false));

    /** Larger sweep slash for scythes; visual only (same textures as vanilla sweep). */
    public static final Supplier<SimpleParticleType> SCYTHE_SWEEP =
            PARTICLE_TYPES.register("scythe_sweep", () -> new SimpleParticleType(false));
}
