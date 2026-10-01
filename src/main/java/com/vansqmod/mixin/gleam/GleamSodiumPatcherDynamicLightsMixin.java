package com.vansqmod.mixin.gleam;

import com.mojang.blaze3d.shaders.Program;
import com.vansqmod.compat.GleamDynamicLightShaders;
import net.thatmaidenjaden.gleam.client.patcher.GleamSodiumPatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GleamSodiumPatcher.class, remap = false)
public abstract class GleamSodiumPatcherDynamicLightsMixin {

    @Inject(method = "applyPatch", at = @At("RETURN"), cancellable = true, remap = false)
    private static void vansqmod$fixDynamicBlend(
            String source,
            Program.Type type,
            CallbackInfoReturnable<String> cir
    ) {
        cir.setReturnValue(GleamDynamicLightShaders.fix(cir.getReturnValue()));
    }
}
