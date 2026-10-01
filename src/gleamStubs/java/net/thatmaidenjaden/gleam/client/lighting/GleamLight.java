package net.thatmaidenjaden.gleam.client.lighting;

/** Compile-only stand-in used when the Gleam jar is not on the classpath. */
public final class GleamLight {

    private final float x, y, z, r, g, b, radius, intensity;
    private final boolean blacklight;
    private final boolean occludeToBlocklight;

    private GleamLight(
            float x, float y, float z, float r, float g, float b, float radius, float intensity,
            boolean blacklight, boolean occludeToBlocklight
    ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
        this.radius = radius;
        this.intensity = intensity;
        this.blacklight = blacklight;
        this.occludeToBlocklight = occludeToBlocklight;
    }

    public static GleamLight create(
            float x, float y, float z, float r, float g, float b, float radius, float intensity,
            boolean blacklight, boolean occludeToBlocklight
    ) {
        return new GleamLight(x, y, z, r, g, b, radius, intensity, blacklight, occludeToBlocklight);
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float z() {
        return z;
    }

    public float r() {
        return r;
    }

    public float g() {
        return g;
    }

    public float b() {
        return b;
    }

    public float radius() {
        return radius;
    }

    public float intensity() {
        return intensity;
    }

    public boolean blacklight() {
        return blacklight;
    }

    public boolean occludeToBlocklight() {
        return occludeToBlocklight;
    }
}
