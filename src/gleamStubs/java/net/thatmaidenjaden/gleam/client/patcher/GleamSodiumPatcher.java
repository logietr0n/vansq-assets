package net.thatmaidenjaden.gleam.client.patcher;

import com.mojang.blaze3d.shaders.Program;

/** Compile-only stand-in used when the Gleam jar is not on the classpath. */
public final class GleamSodiumPatcher {

    private GleamSodiumPatcher() {
    }

    public static String applyPatch(String source, Program.Type type) {
        return source;
    }
}
