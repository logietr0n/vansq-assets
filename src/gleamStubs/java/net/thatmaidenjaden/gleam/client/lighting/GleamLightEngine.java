package net.thatmaidenjaden.gleam.client.lighting;

import net.minecraft.client.renderer.ShaderInstance;

/** Compile-only stand-in used when the Gleam jar is not on the classpath. */
public final class GleamLightEngine {

    // Not final so javac does not inline a stub value into vansqmod classes.
    public static int MAX_TOTAL_LIGHTS = 256;

    private static final GleamLightEngine INSTANCE = new GleamLightEngine();

    private GleamLightEngine() {
    }

    public static synchronized GleamLightEngine getInstance() {
        return INSTANCE;
    }

    public void registerShader(ShaderInstance shader) {
    }

    public void rebindBlocks() {
    }

    public void bindBuffers() {
    }

    public void markDirty() {
    }

    public boolean isDirty() {
        return false;
    }

    public void updateSceneUniform(double x, double y, double z) {
    }
}
