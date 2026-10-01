package com.vansqmod.mixin.tinytakeover;

import com.vansqmod.client.DrownedBabyModels;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;

/**
 * Tiny Takeover treats Gelid/Thicket as zombies, so babies get
 * {@code BabyZombieModel} on {@code zombie_baby}. Use the drowned-baby
 * layer (same unique mesh, drowned UVs) so they match baby drowned.
 */
@Mixin(
        targets = "com.evandev.tiny_takeover_backport.client.ModBabyModelRegistry",
        remap = false
)
public abstract class VvDrownedBabyModelMixin {

    @Inject(method = "createBabyModel", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void vansqmod$gelidThicketDrownedBaby(
            LivingEntityRenderer renderer,
            EntityRendererProvider.Context context,
            EntityModel adult,
            CallbackInfoReturnable cir
    ) {
        if (adult == null) {
            return;
        }
        String name = adult.getClass().getSimpleName();
        if (!"GelidEntityModel".equals(name) && !"ThicketEntityModel".equals(name)) {
            return;
        }
        EntityModel<?> model = vansqmod$babyDrownedModel(context);
        if (model != null) {
            cir.setReturnValue(model);
        }
    }

    private static EntityModel<?> vansqmod$babyDrownedModel(EntityRendererProvider.Context context) {
        ModelPart part = vansqmod$bake(context, new ModelLayerLocation(
                ResourceLocation.fromNamespaceAndPath("tiny_takeover_backport", "drowned_baby"),
                "main"
        ));
        if (part == null) {
            part = vansqmod$bake(context, DrownedBabyModels.BODY);
        }
        if (part == null) {
            return null;
        }
        try {
            Class<?> cls = Class.forName("com.evandev.tiny_takeover_backport.client.model.BabyZombieModel");
            Constructor<?> ctor = cls.getConstructor(ModelPart.class);
            return (EntityModel<?>) ctor.newInstance(part);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static ModelPart vansqmod$bake(EntityRendererProvider.Context context, ModelLayerLocation layer) {
        try {
            return context.getModelSet().bakeLayer(layer);
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}
