package com.vansqmod.mixin.distanthorizons;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Scales cloud alpha after the normal lightmap and face shading, so blindness,
 * darkness, and directional shade still apply.
 */
@Mixin(
        targets = "com.seibel.distanthorizons.common.render.openGl.glObject.shader.GlShader",
        remap = false
)
public abstract class GlShaderCloudColorMixin {

    private static final String ALPHA_UNIFORM = "uniform float uVansqCloudAlpha;\n";

    /**
     * Same distance fog Distant Horizons applies to LODs, plus a test against
     * the DH depth buffer so clouds behind terrain are dropped. Both run only
     * while the transparent cloud layer is drawing.
     */
    private static final String FOG_MAIN = """
            uniform float uVansqCloudLayer;
            uniform float uVansqHasFog;
            uniform float uVansqHasDepth;
            uniform sampler2D uVansqDhDepth;
            uniform vec2 uVansqViewport;
            uniform mat4 uVansqInvMvmProj;
            uniform bool uVansqReverseZ;
            uniform bool uVansqDepthZeroToOne;
            uniform vec4 uVansqFogColor;
            uniform float uVansqFogScale;
            uniform float uVansqFogVerticalScale;
            uniform int uVansqFogFalloffType;
            uniform float uVansqFarFogStart;
            uniform float uVansqFarFogLength;
            uniform float uVansqFarFogMin;
            uniform float uVansqFarFogRange;
            uniform float uVansqFarFogDensity;
            uniform float uVansqHeightFogStart;
            uniform float uVansqHeightFogLength;
            uniform float uVansqHeightFogMin;
            uniform float uVansqHeightFogRange;
            uniform float uVansqHeightFogDensity;
            uniform bool uVansqHeightFogEnabled;
            uniform int uVansqHeightFogFalloffType;
            uniform bool uVansqHeightBasedOnCamera;
            uniform float uVansqHeightFogBaseHeight;
            uniform bool uVansqHeightFogAppliesUp;
            uniform bool uVansqHeightFogAppliesDown;
            uniform bool uVansqUseSphericalFog;
            uniform int uVansqHeightFogMixingMode;
            uniform float uVansqCameraBlockYPos;

            vec3 vansqViewPosition(float fragmentDepth) {
                vec2 uv = gl_FragCoord.xy / uVansqViewport;
                vec4 ndc = vec4(uv, fragmentDepth, 1.0);
                if (uVansqDepthZeroToOne) {
                    ndc.xy = ndc.xy * 2.0 - 1.0;
                } else {
                    ndc.xyz = ndc.xyz * 2.0 - 1.0;
                }
                vec4 eyeCoord = uVansqInvMvmProj * ndc;
                return eyeCoord.xyz / eyeCoord.w;
            }

            float vansqLinearFog(float worldDist, float fogStart, float fogLength, float fogMin, float fogRange) {
                worldDist = (worldDist - fogStart) / fogLength;
                worldDist = clamp(worldDist, 0.0, 1.0);
                return fogMin + fogRange * worldDist;
            }

            float vansqExponentialFog(float x, float fogStart, float fogLength, float fogMin, float fogRange, float fogDensity) {
                x = max((x - fogStart) / fogLength, 0.0) * fogDensity;
                return fogMin + fogRange - fogRange / exp(x);
            }

            float vansqExponentialSquaredFog(float x, float fogStart, float fogLength, float fogMin, float fogRange, float fogDensity) {
                x = max((x - fogStart) / fogLength, 0.0) * fogDensity;
                return fogMin + fogRange - fogRange / exp(x * x);
            }

            float vansqFarFog(float dist) {
                if (uVansqFogFalloffType == 0) {
                    return vansqLinearFog(dist, uVansqFarFogStart, uVansqFarFogLength, uVansqFarFogMin, uVansqFarFogRange);
                } else if (uVansqFogFalloffType == 1) {
                    return vansqExponentialFog(dist, uVansqFarFogStart, uVansqFarFogLength, uVansqFarFogMin, uVansqFarFogRange, uVansqFarFogDensity);
                }
                return vansqExponentialSquaredFog(dist, uVansqFarFogStart, uVansqFarFogLength, uVansqFarFogMin, uVansqFarFogRange, uVansqFarFogDensity);
            }

            float vansqHeightFog(float dist) {
                if (!uVansqHeightFogEnabled) {
                    return 0.0;
                }
                if (uVansqHeightFogFalloffType == 0) {
                    return vansqLinearFog(dist, uVansqHeightFogStart, uVansqHeightFogLength, uVansqHeightFogMin, uVansqHeightFogRange);
                } else if (uVansqHeightFogFalloffType == 1) {
                    return vansqExponentialFog(dist, uVansqHeightFogStart, uVansqHeightFogLength, uVansqHeightFogMin, uVansqHeightFogRange, uVansqHeightFogDensity);
                }
                return vansqExponentialSquaredFog(dist, uVansqHeightFogStart, uVansqHeightFogLength, uVansqHeightFogMin, uVansqHeightFogRange, uVansqHeightFogDensity);
            }

            float vansqHeightDepth(float worldYPos) {
                if (!uVansqHeightFogEnabled) {
                    return 0.0;
                }
                if (!uVansqHeightBasedOnCamera) {
                    worldYPos -= (uVansqHeightFogBaseHeight - uVansqCameraBlockYPos);
                }
                if (uVansqHeightFogAppliesDown && uVansqHeightFogAppliesUp) {
                    return abs(worldYPos) * uVansqFogVerticalScale;
                } else if (uVansqHeightFogAppliesDown) {
                    return -worldYPos * uVansqFogVerticalScale;
                } else if (uVansqHeightFogAppliesUp) {
                    return worldYPos * uVansqFogVerticalScale;
                }
                return 0.0;
            }

            float vansqMixFog(float farFog, float heightFog) {
                switch (uVansqHeightFogMixingMode) {
                    case 0:
                    case 1:
                        return farFog;
                    case 2:
                        return max(farFog, heightFog);
                    case 3:
                        return farFog + heightFog;
                    case 4:
                        return farFog * heightFog;
                    case 5:
                        return 1.0 - (1.0 - farFog) * (1.0 - heightFog);
                    case 6:
                        return farFog + max(farFog, heightFog);
                    case 7:
                        return farFog + farFog * heightFog;
                    case 8:
                        return farFog + 1.0 - (1.0 - farFog) * (1.0 - heightFog);
                    case 9:
                        return farFog * 0.5 + heightFog * 0.5;
                }
                return farFog;
            }

            void main()
            {
                fragColor = fColor;
                if (uVansqCloudLayer < 0.5) {
                    return;
                }
                if (uVansqHasDepth > 0.5 && uVansqViewport.x > 0.0 && uVansqViewport.y > 0.0) {
                    float sceneDepth = texture(uVansqDhDepth, gl_FragCoord.xy / uVansqViewport).r;
                    if (uVansqReverseZ) {
                        if (sceneDepth > 0.0 && gl_FragCoord.z < sceneDepth) {
                            discard;
                        }
                    } else if (sceneDepth < 1.0 && gl_FragCoord.z > sceneDepth) {
                        discard;
                    }
                }
                if (uVansqHasFog > 0.5) {
                    vec3 worldPos = vansqViewPosition(gl_FragCoord.z);
                    float horizontal = length(worldPos.xz) * uVansqFogScale;
                    float spherical = length(worldPos.xyz) * uVansqFogScale;
                    float active = uVansqUseSphericalFog ? spherical : horizontal;
                    float farFog = vansqFarFog(active);
                    float heightFog = vansqHeightFog(vansqHeightDepth(worldPos.y));
                    fragColor.rgb = mix(fragColor.rgb, uVansqFogColor.rgb, clamp(vansqMixFog(farFog, heightFog), 0.0, 1.0));
                }
            }
            """;

    @Inject(method = "loadFile", at = @At("RETURN"), cancellable = true, remap = false)
    private static void vansqmod$cloudLayerAlpha(String path, boolean externalFile, CallbackInfoReturnable<String> cir) {
        if (path == null) {
            return;
        }
        String normalizedPath = path.replace('\\', '/');
        boolean cloudVert = normalizedPath.endsWith("shaders/generic/gl/instanced/vert.vert")
                || normalizedPath.endsWith("shaders/generic/gl/direct/vert.vert");
        boolean cloudFrag = normalizedPath.endsWith("shaders/generic/gl/instanced/frag.frag")
                || normalizedPath.endsWith("shaders/generic/gl/direct/frag.frag");
        if (!cloudVert && !cloudFrag) {
            return;
        }
        String source = cir.getReturnValue();
        if (source == null) {
            return;
        }
        String normalized = source.replace("\r\n", "\n");
        if (cloudVert) {
            if (normalized.contains("uVansqCloudAlpha")) {
                return;
            }
            String shadingEnd = "fColor.rgb *= uTopShading; }";
            if (!normalized.contains("uniform sampler2D uLightMap;") || !normalized.contains(shadingEnd)) {
                return;
            }
            cir.setReturnValue(normalized
                    .replace("uniform sampler2D uLightMap;\n", "uniform sampler2D uLightMap;\n" + ALPHA_UNIFORM)
                    .replace(shadingEnd, shadingEnd + "\n    fColor.a *= uVansqCloudAlpha;"));
            return;
        }
        if (normalized.contains("uVansqCloudLayer") || !normalized.contains("fragColor = fColor;")) {
            return;
        }
        int mainAt = normalized.indexOf("void main()");
        if (mainAt < 0) {
            return;
        }
        cir.setReturnValue(normalized.substring(0, mainAt) + FOG_MAIN);
    }
}
