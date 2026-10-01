package com.vansqmod.mixin.spideroverhaul;

import com.vansqmod.client.SpiderOverhaulEyesLayer;
import com.vansqmod.mixin.LivingEntityRendererAddLayerAccessor;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds a glowing-eye overlay to every Spider Overhaul variant renderer.
 */
@Mixin(targets = {
        "dev.chybx.spideroverhaul.client.render.BirchSpiderRenderer",
        "dev.chybx.spideroverhaul.client.render.CavernSpiderRenderer",
        "dev.chybx.spideroverhaul.client.render.DesertSpiderRenderer",
        "dev.chybx.spideroverhaul.client.render.IceSpiderRenderer",
        "dev.chybx.spideroverhaul.client.render.JungleSpiderRenderer",
        "dev.chybx.spideroverhaul.client.render.MushroomSpiderRenderer",
        "dev.chybx.spideroverhaul.client.render.OceanSpiderRenderer",
        "dev.chybx.spideroverhaul.client.render.SavannaSpiderRenderer",
        "dev.chybx.spideroverhaul.client.render.SculkSpiderRenderer",
        "dev.chybx.spideroverhaul.client.render.SwampSpiderRenderer",
        "dev.chybx.spideroverhaul.client.render.TaigaSpiderRenderer"
})
public abstract class SpiderOverhaulEyesRendererMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void vansqmod$addGlowingEyes(EntityRendererProvider.Context context, CallbackInfo ci) {
        LivingEntityRenderer renderer = (LivingEntityRenderer) (Object) this;
        ((LivingEntityRendererAddLayerAccessor) renderer).vansqmod$addLayer(new SpiderOverhaulEyesLayer(renderer));
    }
}
