package com.vansqmod.mixin.geckolib;

import com.vansqmod.compat.HealLightTint;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.Color;

/**
 * HealLight only wraps {@code LivingEntityRenderer} model draws. Geo bodies (and
 * their baked outline bones) get the green heal tint here instead.
 */
@Mixin(GeoEntityRenderer.class)
public abstract class GeoEntityHealColorMixin {

    @Inject(
            method = "getRenderColor(Lnet/minecraft/world/entity/Entity;FI)Lsoftware/bernie/geckolib/util/Color;",
            at = @At("RETURN"),
            remap = false,
            cancellable = true
    )
    private void vansqmod$healColor(Entity entity, float partialTick, int packedLight, CallbackInfoReturnable<Color> cir) {
        if (!(entity instanceof LivingEntity living) || !HealLightTint.healing(living)) {
            return;
        }
        Color current = cir.getReturnValue();
        int alpha = current != null ? current.getAlpha() : 255;
        cir.setReturnValue(new Color((alpha << 24) | (HealLightTint.COLOR & 0x00FFFFFF)));
    }

    @ModifyVariable(
            method = "render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private int vansqmod$healLight(int packedLight, Entity entity) {
        if (entity instanceof LivingEntity living) {
            return HealLightTint.boostLight(living, packedLight);
        }
        return packedLight;
    }
}
