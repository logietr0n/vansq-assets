package com.vansqmod.mixin.tinytakeover;

import com.vansqmod.client.VvZombieVariantSkins;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tiny Takeover's baby remap only rewrites {@code minecraft:} paths. Gelid/Thicket
 * body textures are {@code variantsandventures:}, so point those lookups at V&amp;V
 * baby sheets.
 */
@Mixin(
        targets = "com.evandev.tiny_takeover_backport.client.ModBabyTextureRegistry",
        remap = false
)
public abstract class VvBabyTextureMixin {

    @Inject(method = "getBabyTexture", at = @At("HEAD"), cancellable = true, remap = false)
    private static void vansqmod$vvBabyTexture(
            LivingEntity entity,
            ResourceLocation original,
            CallbackInfoReturnable<ResourceLocation> cir
    ) {
        ResourceLocation mapped = VvZombieVariantSkins.body(entity, original);
        if (entity.isBaby() && mapped != original) {
            cir.setReturnValue(mapped);
        }
    }
}
