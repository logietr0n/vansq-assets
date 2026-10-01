package com.vansqmod.mixin.creeperoverhaul;

import com.vansqmod.client.CreeperOverhaulEnchantedEyesLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Mixin(targets = "tech.thatgravyboat.creeperoverhaul.client.renderer.normal.CreeperRenderer", remap = false)
public abstract class CreeperOverhaulEnchantedEyesMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void vansqmod$addEnchantedEyes(EntityRendererProvider.Context context, GeoModel model, CallbackInfo ci) {
        GeoEntityRenderer renderer = (GeoEntityRenderer) (Object) this;
        renderer.addRenderLayer(new CreeperOverhaulEnchantedEyesLayer(renderer));
    }
}
