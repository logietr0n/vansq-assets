package com.vansqmod.compat;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FogType;

/**
 * Sound Physics Remastered muffles when {@code LocalPlayer.isUnderWater()} is true
 * (eyes in water) or when the sound source is in water. The listener is the camera,
 * so standing in a pond with the camera above water should stay dry.
 */
public final class SoundPhysicsCamera {

    private SoundPhysicsCamera() {
    }

    public static boolean isListenerSubmergedInWater() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.gameRenderer == null) {
            return false;
        }
        Camera camera = minecraft.gameRenderer.getMainCamera();
        if (camera == null || !camera.isInitialized()) {
            return false;
        }
        if (camera.getFluidInCamera() == FogType.WATER) {
            return true;
        }
        if (minecraft.level == null) {
            return false;
        }
        return minecraft.level.getFluidState(BlockPos.containing(camera.getPosition())).is(FluidTags.WATER);
    }
}
