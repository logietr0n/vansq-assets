package com.vansqmod.mixin.gleam;

import com.mojang.blaze3d.shaders.Program;
import com.vansqmod.compat.GleamDynamicLightShaders;
import com.vansqmod.compat.GleamEntityShaders;
import net.thatmaidenjaden.gleam.client.patcher.GleamVanillaPatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GleamVanillaPatcher.class, remap = false)
public abstract class GleamVanillaPatcherDynamicLightsMixin {

    @Inject(method = "applyPatch", at = @At("RETURN"), cancellable = true, remap = false)
    private static void vansqmod$fixDynamicBlend(
            String source,
            Program.Type type,
            CallbackInfoReturnable<String> cir
    ) {
        cir.setReturnValue(GleamEntityShaders.fix(GleamDynamicLightShaders.fix(cir.getReturnValue()), type));
    }
}
