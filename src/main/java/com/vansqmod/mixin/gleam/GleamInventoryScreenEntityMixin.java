package com.vansqmod.mixin.gleam;

import com.vansqmod.compat.GleamEntityShaders;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Backup for paper-doll fullbright if {@code Lighting.setupForEntityInInventory} is
 * skipped or the no-arg / quaternion overloads fail to match.
 */
@Mixin(InventoryScreen.class)
public abstract class GleamInventoryScreenEntityMixin {

    @Inject(
            method = "renderEntityInInventory(Lnet/minecraft/client/gui/GuiGraphics;FFFLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("HEAD"),
            require = 0
    )
    private static void vansqmod$beginPaperDollLight(CallbackInfo ci) {
        GleamEntityShaders.setGuiEntityLighting(true);
    }

    @Inject(
            method = "renderEntityInInventory(Lnet/minecraft/client/gui/GuiGraphics;FFFLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("RETURN"),
            require = 0
    )
    private static void vansqmod$keepGuiLightAfterPaperDoll(CallbackInfo ci) {
        // Stay in GUI lighting so inventory items after the doll do not pick up world Gleam.
        GleamEntityShaders.setGuiEntityLighting(true);
    }
}
