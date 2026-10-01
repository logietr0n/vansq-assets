package com.vansqmod.mixin.emf;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_model_features.models.jem_objects.EMFPartData;
import traben.entity_model_features.models.parts.EMFModelPartRoot;

/**
 * Replaces Supplementaries {@code CompatEMFMixin}, which still targets EMF 3.1's 4-arg
 * custom-part constructor. Copy jem {@code textureSize} onto the real EMF 3.3 constructor
 * so dragon/warden/piglin CEM and skull blocks keep correct overlay UVs.
 */
@Mixin(
        targets = "traben.entity_model_features.models.parts.EMFModelPartCustom",
        remap = false,
        priority = 1
)
public abstract class EmfModelPartCustomTextureSizeMixin {

    @Inject(
            method = "<init>(Ltraben/entity_model_features/models/jem_objects/EMFPartData;ILjava/lang/String;Ljava/lang/String;Ltraben/entity_model_features/models/parts/EMFModelPartRoot;)V",
            at = @At("RETURN")
    )
    private void vansqmod$copyEmfTextureSize(
            EMFPartData emfPartData,
            int variant,
            String part,
            String id,
            EMFModelPartRoot root,
            CallbackInfo ci
    ) {
        if (emfPartData == null || emfPartData.textureSize == null || emfPartData.textureSize.length < 2) {
            return;
        }
        try {
            this.getClass().getMethod("supp$setDimensions", int.class, int.class)
                    .invoke(this, emfPartData.textureSize[0], emfPartData.textureSize[1]);
        } catch (ReflectiveOperationException ignored) {
            // Supplementaries is optional at compile time; missing API is a no-op.
        }
    }
}
