package com.vansqmod.mixin.soundphysics;

import com.vansqmod.compat.SoundPhysicsCamera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Sound Physics applies its underwater lowpass when the player's eyes are in water
 * or the sound origin is in water. Both fire while wading with the camera still in air.
 * Gate the filter on the camera fluid instead.
 */
@Mixin(targets = "com.sonicether.soundphysics.SoundPhysics", remap = false)
public abstract class SoundPhysicsUnderwaterMixin {

    @Redirect(
            method = "evaluateEnvironment",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;isUnderWater()Z",
                    remap = true
            ),
            remap = false
    )
    private static boolean vansqmod$cameraSubmerged(LocalPlayer player) {
        return SoundPhysicsCamera.isListenerSubmergedInWater();
    }

    @Redirect(
            method = "evaluateEnvironment",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z",
                    remap = true
            ),
            remap = false
    )
    private static boolean vansqmod$ignoreSourceUnderwater(FluidState state, TagKey<Fluid> tag) {
        if (FluidTags.WATER.equals(tag)) {
            return false;
        }
        return state.is(tag);
    }
}
