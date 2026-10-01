package com.vansqmod.mixin.gleam;

import com.mojang.blaze3d.shaders.Uniform;
import com.vansqmod.compat.GleamEntityShaders;
import net.minecraft.client.renderer.ShaderInstance;
import net.thatmaidenjaden.gleam.client.lighting.GleamLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Entity vertex programs recover camera-relative world position with IViewRotMat.
 * The uniform is injected in GLSL only (not the shader JSON), so the location is
 * cached at link time and uploaded after the program is bound.
 */
@Mixin(ShaderInstance.class)
public abstract class GleamShaderIViewRotMatMixin {

    @Shadow
    public abstract int getId();

    @Shadow
    public abstract String getName();

    @Unique
    private int vansqmod$iViewRotMatLocation = -1;

    @Inject(method = "<init>(Lnet/minecraft/server/packs/resources/ResourceProvider;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/VertexFormat;)V", at = @At("RETURN"))
    private void vansqmod$cacheEntityGleam(CallbackInfo ci) {
        this.vansqmod$iViewRotMatLocation = Uniform.glGetUniformLocation(this.getId(), "IViewRotMat");
        if (GleamEntityShaders.isProgram(this.getName())) {
            GleamLightEngine.getInstance().registerShader((ShaderInstance) (Object) this);
        }
    }

    @Inject(method = "apply", at = @At("RETURN"))
    private void vansqmod$uploadIViewRotMat(CallbackInfo ci) {
        if (GleamEntityShaders.isProgram(this.getName())) {
            GleamLightEngine.getInstance().bindBuffers();
        }
        if (this.vansqmod$iViewRotMatLocation >= 0) {
            GleamEntityShaders.uploadViewRotation(this.vansqmod$iViewRotMatLocation);
        }
    }
}
