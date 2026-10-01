package com.vansqmod.mixin.distanthorizons;

import com.vansqmod.client.DhCloudDayNight;
import com.vansqmod.client.DhCloudFog;
import com.vansqmod.client.DhCloudOverlay;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        targets = "com.seibel.distanthorizons.common.render.openGl.generic.GlGenericObjectShaderProgram",
        remap = false
)
public abstract class GlGenericObjectShaderCloudColorMixin {

    @Inject(method = "bind", at = @At("RETURN"), remap = false)
    private void vansqmod$keepFadingCloudsWhite(CallbackInfo ci) {
        int program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        if (program == 0) {
            return;
        }
        uniform1f(program, "uVansqCloudAlpha", DhCloudOverlay.isOverlayPass() ? DhCloudDayNight.layerAlpha() : 1.0F);
        DhCloudFog.apply(program);
    }

    private static void uniform1f(int program, String name, float value) {
        int location = GL20.glGetUniformLocation(program, name);
        if (location >= 0) {
            GL20.glUniform1f(location, value);
        }
    }
}
