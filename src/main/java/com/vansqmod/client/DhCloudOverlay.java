package com.vansqmod.client;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Draws Distant Horizons clouds after DH has copied its frame onto Minecraft.
 * The boxes are depth-tested against each other, fragments behind DH terrain
 * are dropped, and the layer is composited once over the sky.
 */
public final class DhCloudOverlay {

    private static final String VERTEX_SHADER = """
            #version 330 core
            out vec2 vUv;
            void main() {
                vec2 pos = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
                vUv = pos;
                gl_Position = vec4(pos * 2.0 - 1.0, 0.0, 1.0);
            }
            """;

    private static final String FRAGMENT_SHADER = """
            #version 330 core
            uniform sampler2D uLayer;
            in vec2 vUv;
            out vec4 fragColor;
            void main() {
                fragColor = texture(uLayer, vUv);
            }
            """;

    private static boolean overlayPass;
    private static boolean capturing;
    private static Runnable overlayDraw = () -> {
    };

    private static int framebuffer;
    private static int colorTexture;
    private static int depthBuffer;
    private static int lightmapId;
    private static int depthTextureId = -1;
    private static int depthFunc = GL11.GL_LESS;
    private static double clearDepth = 1.0;
    private static double depthRangeNear;
    private static double depthRangeFar = 1.0;
    private static int width;
    private static int height;
    private static int compositeProgram;
    private static int emptyVao;

    private DhCloudOverlay() {
    }

    public static boolean isOverlayPass() {
        return overlayPass;
    }

    public static boolean isCapturing() {
        return capturing;
    }

    public static void setDraw(Runnable draw) {
        overlayDraw = draw != null ? draw : () -> {
        };
    }

    /** Lightmap, depth test, and DH depth texture used for this frame's clouds. */
    public static void notePassState(int lightmap, int depthFunction, double depthClear, int depthTexture, double rangeNear, double rangeFar) {
        if (lightmap != 0) {
            lightmapId = lightmap;
        }
        if (depthTexture > 0) {
            depthTextureId = depthTexture;
        }
        depthRangeNear = rangeNear;
        depthRangeFar = rangeFar;
        if (depthFunction != 0) {
            depthFunc = depthFunction;
            clearDepth = depthFunction == GL11.GL_GREATER || depthFunction == GL11.GL_GEQUAL ? 0.0 : 1.0;
        } else {
            clearDepth = depthClear;
        }
    }

    public static boolean hasDepthTexture() {
        return depthTextureId > 0;
    }

    public static int targetWidth() {
        return width;
    }

    public static int targetHeight() {
        return height;
    }

    public static void draw() {
        if (overlayPass || !DhCloudDayNight.usesLayer()) {
            return;
        }
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        if (viewport[2] <= 0 || viewport[3] <= 0 || !ensureTargets(viewport[2], viewport[3])) {
            return;
        }

        overlayPass = true;
        capturing = true;
        int previousFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int previousProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int previousVao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        int previousActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        int previousDepthBinding = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        boolean scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        int[] previousDepthFunc = new int[1];
        double[] previousClearDepth = new double[1];
        double[] previousDepthRange = new double[2];
        GL11.glGetIntegerv(GL11.GL_DEPTH_FUNC, previousDepthFunc);
        GL11.glGetDoublev(GL11.GL_DEPTH_CLEAR_VALUE, previousClearDepth);
        GL11.glGetDoublev(GL11.GL_DEPTH_RANGE, previousDepthRange);
        int[] equationRgb = new int[1];
        int[] equationAlpha = new int[1];
        int[] srcRgb = new int[1];
        int[] dstRgb = new int[1];
        int[] srcAlpha = new int[1];
        int[] dstAlpha = new int[1];
        float[] clearColor = new float[4];
        GL11.glGetIntegerv(GL20.GL_BLEND_EQUATION_RGB, equationRgb);
        GL11.glGetIntegerv(GL20.GL_BLEND_EQUATION_ALPHA, equationAlpha);
        GL11.glGetIntegerv(GL14.GL_BLEND_SRC_RGB, srcRgb);
        GL11.glGetIntegerv(GL14.GL_BLEND_DST_RGB, dstRgb);
        GL11.glGetIntegerv(GL14.GL_BLEND_SRC_ALPHA, srcAlpha);
        GL11.glGetIntegerv(GL14.GL_BLEND_DST_ALPHA, dstAlpha);
        GL11.glGetFloatv(GL11.GL_COLOR_CLEAR_VALUE, clearColor);
        try {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
            GL11.glViewport(0, 0, width, height);
            GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            GL11.glClearDepth(clearDepth);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(depthFunc);
            GL11.glDepthRange(depthRangeNear, depthRangeFar);
            GL11.glDepthMask(true);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            if (depthTextureId > 0) {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTextureId);
            }
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            if (lightmapId != 0) {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, lightmapId);
            }
            overlayDraw.run();
            capturing = false;

            GL14.glBlendEquation(GL14.GL_FUNC_ADD);
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer);
            GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_BLEND);
            GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL20.glUseProgram(compositeProgram);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture);
            GL30.glBindVertexArray(emptyVao);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
        } finally {
            capturing = false;
            overlayPass = false;
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer);
            GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            GL20.glUseProgram(previousProgram);
            GL30.glBindVertexArray(previousVao);
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousDepthBinding);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);
            GL13.glActiveTexture(previousActiveTexture);
            GL11.glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
            GL11.glClearDepth(previousClearDepth[0]);
            GL11.glDepthRange(previousDepthRange[0], previousDepthRange[1]);
            GL11.glDepthFunc(previousDepthFunc[0]);
            GL20.glBlendEquationSeparate(equationRgb[0], equationAlpha[0]);
            GL14.glBlendFuncSeparate(srcRgb[0], dstRgb[0], srcAlpha[0], dstAlpha[0]);
            GL11.glDepthMask(depthMask);
            setEnabled(GL11.GL_BLEND, blend);
            setEnabled(GL11.GL_DEPTH_TEST, depthTest);
            setEnabled(GL11.GL_SCISSOR_TEST, scissor);
        }
    }

    private static boolean ensureTargets(int targetWidth, int targetHeight) {
        if (compositeProgram == 0 && !createCompositeProgram()) {
            return false;
        }
        if (emptyVao == 0) {
            emptyVao = GL30.glGenVertexArrays();
        }
        if (framebuffer != 0 && width == targetWidth && height == targetHeight) {
            return true;
        }
        deleteTargets();
        width = targetWidth;
        height = targetHeight;
        colorTexture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0L);
        framebuffer = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, colorTexture, 0);
        depthBuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, width, height);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        return status == GL30.GL_FRAMEBUFFER_COMPLETE;
    }

    private static void deleteTargets() {
        if (colorTexture != 0) {
            GL11.glDeleteTextures(colorTexture);
            colorTexture = 0;
        }
        if (depthBuffer != 0) {
            GL30.glDeleteRenderbuffers(depthBuffer);
            depthBuffer = 0;
        }
        if (framebuffer != 0) {
            GL30.glDeleteFramebuffers(framebuffer);
            framebuffer = 0;
        }
    }

    private static boolean createCompositeProgram() {
        int vertex = compile(GL20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragment = compile(GL20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        if (vertex == 0 || fragment == 0) {
            GL20.glDeleteShader(vertex);
            GL20.glDeleteShader(fragment);
            return false;
        }
        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertex);
        GL20.glAttachShader(program, fragment);
        GL20.glLinkProgram(program);
        GL20.glDeleteShader(vertex);
        GL20.glDeleteShader(fragment);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            GL20.glDeleteProgram(program);
            return false;
        }
        GL20.glUseProgram(program);
        int layer = GL20.glGetUniformLocation(program, "uLayer");
        if (layer >= 0) {
            GL20.glUniform1i(layer, 0);
        }
        GL20.glUseProgram(0);
        compositeProgram = program;
        return true;
    }

    private static int compile(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            GL20.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }

    private static void setEnabled(int cap, boolean enabled) {
        if (enabled) {
            GL11.glEnable(cap);
        } else {
            GL11.glDisable(cap);
        }
    }
}
