package com.vansqmod.mixin.sodium;

import com.vansqmod.compat.SodiumSkyLight;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Restore sky light on opaque interiors from the air above. Handheld dynamic light
 * used to write block-only values into those interiors (sky=0), and Sodium's smooth
 * lighting interpolated that into daylight faces as dark patches.
 * <p>
 * Also copy the block's light emission into the stored block-light nibble. Lava,
 * glowstone, and magma keep emission in a separate field that AoFaceData ignores,
 * so pit walls against lava were smoothed as if the neighbor were unlit.
 */
@Mixin(
        targets = "net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess",
        remap = false
)
public abstract class SodiumDynamicLightDataMixin {

    @Shadow
    protected BlockAndTintGetter level;

    @Inject(method = "compute", at = @At("RETURN"), cancellable = true, remap = false)
    private void vansqmod$restoreSkyLight(int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
        int word = cir.getReturnValue();
        int block = Math.max(word & 15, word >> 8 & 15);
        int sky = SodiumSkyLight.maxWorldSky(this.level, x, y, z, word >> 4 & 15);
        int rebuilt = (word & ~0xFF) | block | sky << 4;
        if (rebuilt != word) {
            cir.setReturnValue(rebuilt);
        }
    }
}
