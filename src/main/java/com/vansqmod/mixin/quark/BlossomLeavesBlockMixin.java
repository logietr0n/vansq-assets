package com.vansqmod.mixin.quark;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Quark blossom ("trumpet") leaves spawn {@code ParticleTypes.BLOCK} crumbs as a falling-leaf
 * stand-in. vansqmod + VanillaBackport already provide real leaf particles, so suppress Quark's.
 * {@code super.animateTick} still runs afterward for VB's falling-leaves hook.
 */
@Mixin(targets = "org.violetmoon.quark.content.world.block.BlossomLeavesBlock", remap = false)
public class BlossomLeavesBlockMixin {

    @Redirect(
            method = "animateTick",
            at = @At(
                    value = "FIELD",
                    target = "Lorg/violetmoon/quark/content/world/module/BlossomTreesModule;dropLeafParticles:Z",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private boolean vansqmod$disableQuarkBlockBreakLeafParticles() {
        return false;
    }
}
