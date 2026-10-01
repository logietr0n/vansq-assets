package com.vansqmod.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vansqmod.client.SkeletonBabyGeoRenderer;
import com.vansqmod.entity.Mellowed;
import com.vansqmod.entity.SkeletonBabies;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.animatable.GeoAnimatable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntitySkeletonBabyRenderMixin {

    @Unique
    private EntityRendererProvider.Context vansqmod$context;

    @Unique
    private SkeletonBabyGeoRenderer<?> vansqmod$babyRenderer;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void vansqmod$captureContext(
            EntityRendererProvider.Context context,
            net.minecraft.client.model.EntityModel<?> model,
            float shadowRadius,
            CallbackInfo ci
    ) {
        this.vansqmod$context = context;
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void vansqmod$renderGeoBaby(
            LivingEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            CallbackInfo ci
    ) {
        if (!(entity instanceof AbstractSkeleton) || entity instanceof Mellowed || !SkeletonBabies.usesGeo(entity)) {
            return;
        }
        if (!(entity instanceof GeoAnimatable) || this.vansqmod$context == null) {
            return;
        }
        vansqmod$babyRenderer().render(
                entity,
                entityYaw,
                partialTick,
                poseStack,
                bufferSource,
                packedLight);
        ci.cancel();
    }

    @Unique
    @SuppressWarnings({"rawtypes", "unchecked"})
    private SkeletonBabyGeoRenderer vansqmod$babyRenderer() {
        if (this.vansqmod$babyRenderer == null) {
            this.vansqmod$babyRenderer = new SkeletonBabyGeoRenderer(this.vansqmod$context);
        }
        return this.vansqmod$babyRenderer;
    }
}
