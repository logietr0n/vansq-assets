package com.vansqmod.mixin.gleam;

import com.mojang.blaze3d.platform.Lighting;
import com.vansqmod.compat.GleamEntityShaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Inventory paper dolls and GUI items use entity shaders with a GUI model-view
 * matrix. Zero the view-rotation uniform there so Gleam skips the world light-grid
 * lookup. World and first-person hands use {@code setupLevel} and keep Gleam.
 */
@Mixin(Lighting.class)
public abstract class GleamLightingGuiEntityMixin {

    @Inject(method = "setupForEntityInInventory()V", at = @At("HEAD"), require = 0)
    private static void vansqmod$beginGuiEntityLighting(CallbackInfo ci) {
        GleamEntityShaders.setGuiEntityLighting(true);
    }

    @Inject(method = "setupForEntityInInventory(Lorg/joml/Quaternionf;)V", at = @At("HEAD"), require = 0)
    private static void vansqmod$beginGuiEntityLightingOriented(CallbackInfo ci) {
        GleamEntityShaders.setGuiEntityLighting(true);
    }

    @Inject(method = "setupFor3DItems", at = @At("HEAD"), require = 0)
    private static void vansqmod$beginGuiItemLighting3d(CallbackInfo ci) {
        GleamEntityShaders.setGuiEntityLighting(true);
    }

    @Inject(method = "setupForFlatItems", at = @At("HEAD"), require = 0)
    private static void vansqmod$beginGuiItemLightingFlat(CallbackInfo ci) {
        GleamEntityShaders.setGuiEntityLighting(true);
    }

    @Inject(method = "setupLevel", at = @At("HEAD"), require = 0)
    private static void vansqmod$endGuiEntityLightingLevel(CallbackInfo ci) {
        GleamEntityShaders.setGuiEntityLighting(false);
    }

    @Inject(method = "setupNetherLevel", at = @At("HEAD"), require = 0)
    private static void vansqmod$endGuiEntityLightingNether(CallbackInfo ci) {
        GleamEntityShaders.setGuiEntityLighting(false);
    }
}
