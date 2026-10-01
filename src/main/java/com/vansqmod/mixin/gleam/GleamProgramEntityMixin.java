package com.vansqmod.mixin.gleam;

import com.mojang.blaze3d.shaders.Program;
import com.vansqmod.compat.GleamEntityShaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.InputStream;

/**
 * Gleam's vanilla patcher only lists block rendertypes. Run the same GPU light
 * injection on entity programs, then rewrite {@code pos} from model space.
 */
@Mixin(Program.class)
public abstract class GleamProgramEntityMixin {

    @ModifyVariable(method = "compileShaderInternal", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static InputStream vansqmod$patchEntityPrograms(InputStream stream, Program.Type type, String name) {
        return GleamEntityShaders.patchProgram(stream, type, name);
    }
}
