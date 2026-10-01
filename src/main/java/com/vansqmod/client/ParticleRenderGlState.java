package com.vansqmod.client;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

/**
 * Pickup particles render entities through {@code MultiBufferSource.endBatch()}.
 * That leaves depth testing off, turns the lightmap off, and on Fabulous graphics
 * rebinds the main target instead of the particles target. Mist then vanishes for
 * the few ticks the pickup animation lasts.
 */
public final class ParticleRenderGlState {
    private static boolean captured;
    private static int savedFramebuffer;
    private static LightTexture lightTexture;

    private ParticleRenderGlState() {
    }

    public static void capturePass(LightTexture texture) {
        captured = true;
        lightTexture = texture;
        savedFramebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
    }

    public static void restoreForParticles() {
        if (captured) {
            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, savedFramebuffer);
        }
        LightTexture lights = lightTexture;
        if (lights == null) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.gameRenderer != null) {
                lights = minecraft.gameRenderer.lightTexture();
            }
        }
        if (lights != null) {
            lights.turnOnLightLayer();
        }
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GlConst.GL_LEQUAL);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void endPass() {
        captured = false;
        lightTexture = null;
        savedFramebuffer = 0;
    }
}
