package com.vansqmod.mixin.moonlight;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Dummy mixin so the config plugin can add {@code ColorUtils.RGB_CODEC} after apply.
 * Dummmmmmy 2.1.2 still reads that field; Moonlight 3.3.0 renamed it to {@code CODEC}.
 */
@Mixin(targets = "net.mehvahdjukaar.moonlight.api.util.math.ColorUtils", remap = false)
public abstract class MoonlightColorUtilsRgbCodecMixin {
}
