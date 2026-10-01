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

    public static final Supplier<SimpleParticleType> ASPEN_LEAVES =
            PARTICLE_TYPES.register("aspen_leaves", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> ROTTEN_LEAVES =
            PARTICLE_TYPES.register("rotten_leaves", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> ECHO_LEAVES =
            PARTICLE_TYPES.register("echo_leaves", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> OPAL_LEAVES =
            PARTICLE_TYPES.register("opal_leaves", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> BLUE_BLOSSOM_LEAVES =
            PARTICLE_TYPES.register("blue_blossom_leaves", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> LAVENDER_BLOSSOM_LEAVES =
            PARTICLE_TYPES.register("lavender_blossom_leaves", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> ORANGE_BLOSSOM_LEAVES =
            PARTICLE_TYPES.register("orange_blossom_leaves", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> RED_BLOSSOM_LEAVES =
            PARTICLE_TYPES.register("red_blossom_leaves", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> YELLOW_BLOSSOM_LEAVES =
            PARTICLE_TYPES.register("yellow_blossom_leaves", () -> new SimpleParticleType(false));

    public static final Supplier<SimpleParticleType> ICE_FLAKE =
            PARTICLE_TYPES.register("ice_flake", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> SOUL_FIREBALL_TRAIL =
            PARTICLE_TYPES.register("soul_fireball_trail", () -> new SimpleParticleType(false));
}
