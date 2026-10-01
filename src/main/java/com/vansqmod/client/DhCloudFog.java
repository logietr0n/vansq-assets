package com.vansqmod.client;

import com.seibel.distanthorizons.api.enums.rendering.EDhApiHeightFogMixMode;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiFogRenderParam;
import com.seibel.distanthorizons.api.objects.math.DhApiMat4f;
import org.lwjgl.opengl.GL20;

import java.awt.Color;

/**
 * Copies Distant Horizons' fog settings in Java and reapplies that same fog
 * to the transparent cloud layer. The fog shader's GL uniforms are left alone.
 */
public final class DhCloudFog {

    private static boolean captured;
    private static final float[] inverseMvmProj = new float[16];
    private static final float[] fogColor = new float[4];
    private static int reverseZ;
    private static int depthZeroToOne;
    private static float fogScale;
    private static float fogVerticalScale;
    private static int fogFalloffType;
    private static float farFogStart;
    private static float farFogLength;
    private static float farFogMin;
    private static float farFogRange;
    private static float farFogDensity;
    private static float heightFogStart;
    private static float heightFogLength;
    private static float heightFogMin;
    private static float heightFogRange;
    private static float heightFogDensity;
    private static int heightFogEnabled;
    private static int heightFogFalloffType;
    private static int heightBasedOnCamera;
    private static float heightFogBaseHeight;
    private static int heightFogAppliesUp;
    private static int heightFogAppliesDown;
    private static int useSphericalFog;
    private static int heightFogMixingMode;
    private static float cameraBlockYPos;

    private DhCloudFog() {
    }

    /**
     * Values match {@code GlDhFogShader.onApplyUniforms}, including its use of
     * the far-fog thickness for the height-fog min, range, and density.
     */
    public static void capture(
            DhApiMat4f inverseMvmProjMatrix,
            DhApiFogRenderParam fog,
            float scale,
            float verticalScale,
            boolean reverseDepth,
            boolean zeroToOneDepth,
            float cameraY
    ) {
        if (inverseMvmProjMatrix == null || fog == null || fog.getFogColor() == null) {
            return;
        }
        storeMatrix(inverseMvmProjMatrix);
        Color color = fog.getFogColor();
        fogColor[0] = color.getRed() / 256.0F;
        fogColor[1] = color.getGreen() / 256.0F;
        fogColor[2] = color.getBlue() / 256.0F;
        fogColor[3] = color.getAlpha() / 256.0F;
        reverseZ = reverseDepth ? 1 : 0;
        depthZeroToOne = zeroToOneDepth ? 1 : 0;
        fogScale = scale;
        fogVerticalScale = verticalScale;
        fogFalloffType = fog.getFarFogFalloff().value;
        farFogStart = fog.getFarFogStartPercent();
        farFogLength = fog.getFarFogEndPercent() - fog.getFarFogStartPercent();
        farFogMin = fog.getFarFogMinThickness();
        farFogRange = fog.getFarFogMaxThickness() - fog.getFarFogMinThickness();
        farFogDensity = fog.getFarFogDensity();
        heightFogStart = fog.getHeightFogStartPercent();
        heightFogLength = fog.getHeightFogEndPercent() - fog.getHeightFogStartPercent();
        heightFogMin = fog.getFarFogMinThickness();
        heightFogRange = fog.getFarFogMaxThickness() - fog.getFarFogMinThickness();
        heightFogDensity = fog.getFarFogDensity();
        EDhApiHeightFogMixMode mixMode = fog.getHeightFogMixingMode();
        heightFogEnabled = mixMode != EDhApiHeightFogMixMode.SPHERICAL && mixMode != EDhApiHeightFogMixMode.CYLINDRICAL ? 1 : 0;
        heightFogFalloffType = fog.getHeightFogFalloff().value;
        heightBasedOnCamera = fog.getHeightFogDirection().basedOnCamera ? 1 : 0;
        heightFogBaseHeight = fog.getHeightFogBaseHeight();
        heightFogAppliesUp = fog.getHeightFogDirection().fogAppliesUp ? 1 : 0;
        heightFogAppliesDown = fog.getHeightFogDirection().fogAppliesDown ? 1 : 0;
        useSphericalFog = mixMode == EDhApiHeightFogMixMode.SPHERICAL ? 1 : 0;
        heightFogMixingMode = mixMode.value;
        cameraBlockYPos = cameraY;
        captured = true;
    }

    private static void storeMatrix(DhApiMat4f matrix) {
        inverseMvmProj[0] = matrix.m00;
        inverseMvmProj[1] = matrix.m10;
        inverseMvmProj[2] = matrix.m20;
        inverseMvmProj[3] = matrix.m30;
        inverseMvmProj[4] = matrix.m01;
        inverseMvmProj[5] = matrix.m11;
        inverseMvmProj[6] = matrix.m21;
        inverseMvmProj[7] = matrix.m31;
        inverseMvmProj[8] = matrix.m02;
        inverseMvmProj[9] = matrix.m12;
        inverseMvmProj[10] = matrix.m22;
        inverseMvmProj[11] = matrix.m32;
        inverseMvmProj[12] = matrix.m03;
        inverseMvmProj[13] = matrix.m13;
        inverseMvmProj[14] = matrix.m23;
        inverseMvmProj[15] = matrix.m33;
    }

    public static void apply(int program) {
        boolean layer = DhCloudOverlay.isOverlayPass();
        uniform1f(program, "uVansqCloudLayer", layer ? 1.0F : 0.0F);
        uniform1f(program, "uVansqHasFog", layer && captured ? 1.0F : 0.0F);
        uniform1f(program, "uVansqHasDepth", layer && DhCloudOverlay.hasDepthTexture() ? 1.0F : 0.0F);
        uniform1i(program, "uVansqDhDepth", 1);
        uniform2f(program, "uVansqViewport", DhCloudOverlay.targetWidth(), DhCloudOverlay.targetHeight());
        if (!layer || !captured) {
            return;
        }
        int inverse = GL20.glGetUniformLocation(program, "uVansqInvMvmProj");
        if (inverse >= 0) {
            GL20.glUniformMatrix4fv(inverse, false, inverseMvmProj);
        }
        int color = GL20.glGetUniformLocation(program, "uVansqFogColor");
        if (color >= 0) {
            GL20.glUniform4fv(color, fogColor);
        }
        uniform1i(program, "uVansqReverseZ", reverseZ);
        uniform1i(program, "uVansqDepthZeroToOne", depthZeroToOne);
        uniform1f(program, "uVansqFogScale", fogScale);
        uniform1f(program, "uVansqFogVerticalScale", fogVerticalScale);
        uniform1i(program, "uVansqFogFalloffType", fogFalloffType);
        uniform1f(program, "uVansqFarFogStart", farFogStart);
        uniform1f(program, "uVansqFarFogLength", farFogLength);
        uniform1f(program, "uVansqFarFogMin", farFogMin);
        uniform1f(program, "uVansqFarFogRange", farFogRange);
        uniform1f(program, "uVansqFarFogDensity", farFogDensity);
        uniform1f(program, "uVansqHeightFogStart", heightFogStart);
        uniform1f(program, "uVansqHeightFogLength", heightFogLength);
        uniform1f(program, "uVansqHeightFogMin", heightFogMin);
        uniform1f(program, "uVansqHeightFogRange", heightFogRange);
        uniform1f(program, "uVansqHeightFogDensity", heightFogDensity);
        uniform1i(program, "uVansqHeightFogEnabled", heightFogEnabled);
        uniform1i(program, "uVansqHeightFogFalloffType", heightFogFalloffType);
        uniform1i(program, "uVansqHeightBasedOnCamera", heightBasedOnCamera);
        uniform1f(program, "uVansqHeightFogBaseHeight", heightFogBaseHeight);
        uniform1i(program, "uVansqHeightFogAppliesUp", heightFogAppliesUp);
        uniform1i(program, "uVansqHeightFogAppliesDown", heightFogAppliesDown);
        uniform1i(program, "uVansqUseSphericalFog", useSphericalFog);
        uniform1i(program, "uVansqHeightFogMixingMode", heightFogMixingMode);
        uniform1f(program, "uVansqCameraBlockYPos", cameraBlockYPos);
    }

    private static void uniform1f(int program, String name, float value) {
        int location = GL20.glGetUniformLocation(program, name);
        if (location >= 0) {
            GL20.glUniform1f(location, value);
        }
    }

    private static void uniform1i(int program, String name, int value) {
        int location = GL20.glGetUniformLocation(program, name);
        if (location >= 0) {
            GL20.glUniform1i(location, value);
        }
    }

    private static void uniform2f(int program, String name, float x, float y) {
        int location = GL20.glGetUniformLocation(program, name);
        if (location >= 0) {
            GL20.glUniform2f(location, x, y);
        }
    }
}
