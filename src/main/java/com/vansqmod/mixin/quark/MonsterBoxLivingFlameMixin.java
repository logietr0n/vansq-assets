package com.vansqmod.mixin.quark;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.yirmiri.dungeonsdelight.DDConfigClient;
import net.yirmiri.dungeonsdelight.core.registry.DDParticles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Quark Monster Box idle flames → Dungeons Delight {@code living_flame}, matching vanilla spawners.
 * Break-progress {@link ParticleTypes#LARGE_SMOKE} is left unchanged (same as DD's spawner smoke).
 */
@Mixin(targets = "org.violetmoon.quark.content.world.block.be.MonsterBoxBlockEntity", remap = false)
public abstract class MonsterBoxLivingFlameMixin {

    @ModifyArg(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
                    remap = true
            ),
            index = 0
    )
    private static ParticleOptions vansqmod$replaceIdleFlame(ParticleOptions original) {
        if (original == ParticleTypes.FLAME && DDConfigClient.SPAWNERS_EMIT_GREEN_FLAMES.get()) {
            return DDParticles.LIVING_FLAME.get();
        }
        return original;
    }
}
