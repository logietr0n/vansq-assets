package com.vansqmod.mixin.variantsandventures;

import com.vansqmod.client.VvZombieVariantSkins;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Variants & Ventures renderers always return the adult body sheet. Point
 * Gelid/Thicket babies at V&amp;V's own {@code *_baby.png} textures.
 */
@Mixin(targets = {
        "com.faboslav.variantsandventures.common.client.render.entity.GelidEntityRenderer",
        "com.faboslav.variantsandventures.common.client.render.entity.ThicketEntityRenderer"
})
public abstract class VvZombieVariantTextureMixin {

    @Inject(method = "getTextureLocation", at = @At("RETURN"), cancellable = true, order = 2000)
    private void vansqmod$bodyTexture(@Coerce Object entity, CallbackInfoReturnable<ResourceLocation> cir) {
        if (entity instanceof Entity living) {
            cir.setReturnValue(VvZombieVariantSkins.body(living, cir.getReturnValue()));
        }
    }
}
