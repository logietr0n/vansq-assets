package com.vansqmod.mixin.gleam;

import com.vansqmod.compat.GleamLambDynamicLights;
import net.thatmaidenjaden.gleam.client.lighting.GleamLight;
import net.thatmaidenjaden.gleam.client.lighting.GleamLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

/**
 * Gleam's per-section scan only sees placed blocks. Append LambDynamicLights
 * entity/item emitters so handheld torches keep Gleam color on Sodium.
 */
@Mixin(value = GleamLightEngine.class, remap = false)
public abstract class GleamLightEngineDynamicLightsMixin {

    @ModifyVariable(method = "uploadLights", at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = false)
    private List<GleamLight> vansqmod$mergeDynamicLights(List<GleamLight> lights) {
        return GleamLambDynamicLights.merge(lights);
    }
}
