package com.vansqmod.mixin.astronomy;

import com.vansqmod.client.AstronomyClientSync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "com.nettakrim.spyglass_astronomy.commands.admin_subcommands.StarCountCommand", remap = false)
public abstract class AstronomyStarCountCommandMixin {

    @ModifyConstant(method = "resetStarCount", constant = @Constant(intValue = 1024))
    private static int vansqmod$resetToDoubledDefault(int original) {
        return AstronomyClientSync.DEFAULT_STAR_COUNT;
    }
}
