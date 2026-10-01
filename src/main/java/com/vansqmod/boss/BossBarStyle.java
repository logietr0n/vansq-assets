package com.vansqmod.boss;

import net.minecraft.world.BossEvent;

/**
 * Visual style for a vansq-owned boss health bar. Vanilla only has seven named
 * bar colors; {@link #colorRgb()} is applied as a client tint on the white sprites.
 */
public record BossBarStyle(int colorRgb, BossEvent.BossBarOverlay overlay) {
}
