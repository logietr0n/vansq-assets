package com.vansqmod.compat;

import com.vansqmod.registry.ModParticleTypes;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Maps third-party leaf blocks to dedicated falling-leaf particles.
 * Dappled Up poplars reuse that mod's particle types through Vanilla Backport's client spawn path.
 */
public final class CustomFallingLeaves {

    private static final Map<ResourceLocation, Supplier<SimpleParticleType>> BY_BLOCK = new HashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> FOREIGN_PARTICLES = new HashMap<>();

    static {
        register("artistry", "aspen_leaves", ModParticleTypes.ASPEN_LEAVES);
        register("artistry", "rotten_leaves", ModParticleTypes.ROTTEN_LEAVES);
        register("deeperdarker", "echo_leaves", ModParticleTypes.ECHO_LEAVES);
        register("galosphere", "opal_leaves", ModParticleTypes.OPAL_LEAVES);
        register("quark", "blue_blossom_leaves", ModParticleTypes.BLUE_BLOSSOM_LEAVES);
        register("quark", "lavender_blossom_leaves", ModParticleTypes.LAVENDER_BLOSSOM_LEAVES);
        register("quark", "orange_blossom_leaves", ModParticleTypes.ORANGE_BLOSSOM_LEAVES);
        register("quark", "red_blossom_leaves", ModParticleTypes.RED_BLOSSOM_LEAVES);
        register("quark", "yellow_blossom_leaves", ModParticleTypes.YELLOW_BLOSSOM_LEAVES);
        registerForeign("dappled_up", "orange_poplar_leaves", "orange_poplar_leaf_particle");
        registerForeign("dappled_up", "red_poplar_leaves", "red_poplar_leaf_particle");
        registerForeign("dappled_up", "yellow_poplar_leaves", "yellow_poplar_leaf_particle");
    }

    private CustomFallingLeaves() {
    }

    private static void register(String namespace, String path, Supplier<SimpleParticleType> particle) {
        BY_BLOCK.put(ResourceLocation.fromNamespaceAndPath(namespace, path), particle);
    }

    private static void registerForeign(String namespace, String blockPath, String particlePath) {
        FOREIGN_PARTICLES.put(
                ResourceLocation.fromNamespaceAndPath(namespace, blockPath),
                ResourceLocation.fromNamespaceAndPath(namespace, particlePath)
        );
    }

    public static SimpleParticleType getParticle(BlockState state) {
        ResourceLocation id = state.getBlock().builtInRegistryHolder().key().location();
        Supplier<SimpleParticleType> supplier = BY_BLOCK.get(id);
        if (supplier != null) {
            return supplier.get();
        }
        ResourceLocation particleId = FOREIGN_PARTICLES.get(id);
        if (particleId == null) {
            return null;
        }
        ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.getOptional(particleId).orElse(null);
        return type instanceof SimpleParticleType simple ? simple : null;
    }
}
