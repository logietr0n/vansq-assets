package com.vansqmod.mixin.astronomy;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Constellation lines were saved as hidden, so finished constellations never
 * drew. Line ribbons also face away from the camera, so they are drawn with
 * face culling off. Width and vertex alpha stay at Spyglass Astronomy's values.
 */
@Mixin(targets = "com.nettakrim.spyglass_astronomy.SpaceRenderingManager", remap = false)
public abstract class AstronomyConstellationRenderMixin {

    private static final String LINES_RESTORED = "vansq-constellation-lines-enabled";

    @Shadow(remap = false)
    private static boolean constellationsVisible;

    @Shadow(remap = false)
    private VertexBuffer constellationsBuffer;

    @Shadow(remap = false)
    private VertexBuffer drawingConstellationsBuffer;

    @Shadow(remap = false)
    public abstract void saveData();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void vansqmod$showSavedConstellationLines(CallbackInfo ci) {
        Path folder = Minecraft.getInstance().gameDirectory.toPath().resolve(".spyglass_astronomy");
        Path marker = folder.resolve(LINES_RESTORED);
        if (Files.exists(marker)) {
            return;
        }
        constellationsVisible = true;
        this.saveData();
        try {
            Files.createDirectories(folder);
            Files.writeString(marker, "1");
        } catch (IOException ignored) {
        }
    }

    @Redirect(
            method = "Render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;drawWithShader(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/ShaderInstance;)V",
                    remap = true
            )
    )
    private void vansqmod$drawConstellationLines(VertexBuffer buffer, Matrix4f modelView, Matrix4f projection, ShaderInstance shader) {
        if (buffer != this.constellationsBuffer && buffer != this.drawingConstellationsBuffer) {
            buffer.drawWithShader(modelView, projection, shader);
            return;
        }
        boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        try {
            buffer.drawWithShader(modelView, projection, shader);
        } finally {
            setEnabled(GL11.GL_CULL_FACE, cull);
            setEnabled(GL11.GL_DEPTH_TEST, depth);
            if (cull) {
                RenderSystem.enableCull();
            } else {
                RenderSystem.disableCull();
            }
            if (depth) {
                RenderSystem.enableDepthTest();
            } else {
                RenderSystem.disableDepthTest();
            }
        }
    }

    private static void setEnabled(int cap, boolean enabled) {
        if (enabled) {
            GL11.glEnable(cap);
        } else {
            GL11.glDisable(cap);
        }
    }
}
