package com.vansqmod.mixin.jade;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vansqmod.client.SpyglassZooming;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.impl.ui.BoxElement;
import snownee.jade.overlay.OverlayRenderer;

/**
 * Spyglass overlay writes depth and disables blend. Jade then draws at z≈1 with
 * depth still on, so its text and icons fail the depth test. Restore GUI state
 * and lift the tooltip above the scope layer. Position is left unchanged.
 */
@Mixin(OverlayRenderer.class)
public abstract class JadeSpyglassOverlayMixin {

    @Inject(
            method = "renderOverlay(Lsnownee/jade/impl/ui/BoxElement;Lnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
                    shift = At.Shift.AFTER
            )
    )
    private static void vansqmod$drawAboveSpyglass(BoxElement element, GuiGraphics graphics, CallbackInfo ci) {
        if (!SpyglassZooming.isZooming()) {
            return;
        }
        graphics.flush();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Lighting.setupForFlatItems();
        graphics.pose().translate(0.0F, 0.0F, 4000.0F);
    }
}
