package net.thatmaidenjaden.gleam.client.lighting;

import net.minecraft.world.level.block.state.BlockState;

/** Compile-only stand-in used when the Gleam jar is not on the classpath. */
public final class GleamEmitterRegistry {

    private GleamEmitterRegistry() {
    }

    public static boolean isEmitter(BlockState state) {
        return false;
    }

    public static GleamLight createLight(BlockState state, int x, int y, int z) {
        return null;
    }
}
