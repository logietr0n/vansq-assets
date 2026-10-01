package com.vansqmod.mixin.vanillabackport;

import com.blackgear.vanillabackport.client.api.renderer.AbstractVariantRenderer;
import com.vansqmod.client.RareChickenModels;
import com.vansqmod.entity.RareChickenVariants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(value = AbstractVariantRenderer.class, remap = false)
public abstract class RareChickenModelMixin {

    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void vansqmod$rareChickenModel(LivingEntity entity, CallbackInfoReturnable<Optional<?>> cir) {
        if (!(entity instanceof Chicken chicken) || !RareChickenVariants.isRare(chicken)) {
            return;
        }
        if (chicken.isBaby() && ModList.get().isLoaded("tiny_takeover_backport")) {
            cir.setReturnValue(Optional.of(RareChickenModels.INSTANCE.getBaby()));
            return;
        }
        cir.setReturnValue(Optional.of(RareChickenModels.INSTANCE.get()));
    }

    @Inject(method = "getTexture(Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private void vansqmod$rareChickenBabyTexture(LivingEntity entity, CallbackInfoReturnable<Optional<ResourceLocation>> cir) {
        if (entity instanceof Chicken chicken
                && chicken.isBaby()
                && RareChickenVariants.isRare(chicken)
                && ModList.get().isLoaded("tiny_takeover_backport")) {
            cir.setReturnValue(Optional.of(RareChickenModels.BABY_TEXTURE));
        }
    }
}
