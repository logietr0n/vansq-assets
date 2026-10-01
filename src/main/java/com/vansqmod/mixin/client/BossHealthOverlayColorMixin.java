package com.vansqmod.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vansqmod.boss.BossBarColorTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tints vanilla white boss-bar sprites with the RGB sent for vansq-owned bars.
 */
@Mixin(BossHealthOverlay.class)
public abstract class BossHealthOverlayColorMixin {

    @Inject(
            method = "drawBar(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/world/BossEvent;)V",
            at = @At("HEAD")
    )
    private void vansqmod$tintBossBar(GuiGraphics graphics, int x, int y, BossEvent event, CallbackInfo ci) {
        Integer rgb = BossBarColorTracker.get(event.getId());
        if (rgb == null) {
            return;
        }
        float red = ((rgb >> 16) & 0xFF) / 255.0F;
        float green = ((rgb >> 8) & 0xFF) / 255.0F;
        float blue = (rgb & 0xFF) / 255.0F;
        graphics.setColor(red, green, blue, 1.0F);
        RenderSystem.setShaderColor(red, green, blue, 1.0F);
    }

    @Inject(
            method = "drawBar(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/world/BossEvent;)V",
            at = @At("RETURN")
    )
    private void vansqmod$resetBossBarTint(GuiGraphics graphics, int x, int y, BossEvent event, CallbackInfo ci) {
        if (BossBarColorTracker.get(event.getId()) == null) {
            return;
        }
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
