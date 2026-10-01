package com.vansqmod.mixin.vanillabackport;

import com.blackgear.vanillabackport.common.level.blocks.FireflyBushBlock;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vanilla Backport's firefly bush spawns {@code vanillabackport:firefly}. Swap that
 * for Subtle Effects' firefly particle so bushes match ambient SE fireflies.
 */
@Mixin(FireflyBushBlock.class)
public abstract class FireflyBushSubtleEffectsMixin {

    @Unique
    private static final ResourceLocation VANSQMOD$SE_FIREFLY =
            ResourceLocation.fromNamespaceAndPath("subtle_effects", "firefly");

    @Redirect(
            method = "animateTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
            )
    )
    private void vansqmod$subtleEffectsFirefly(
            Level level,
            ParticleOptions original,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed
    ) {
        ParticleOptions particle = vansqmod$subtleFirefly();
        level.addParticle(particle != null ? particle : original, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Unique
    private static ParticleOptions vansqmod$subtleFirefly() {
        try {
            var type = BuiltInRegistries.PARTICLE_TYPE.get(VANSQMOD$SE_FIREFLY);
            return type instanceof SimpleParticleType simple ? simple : null;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
