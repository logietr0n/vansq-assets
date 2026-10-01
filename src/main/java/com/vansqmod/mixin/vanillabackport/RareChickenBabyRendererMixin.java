package com.vansqmod.mixin.vanillabackport;

import com.vansqmod.client.RareChickenModels;
import com.vansqmod.entity.RareChickenVariants;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class RareChickenBabyRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow
    protected M model;

    private static final ThreadLocal<EntityModel<?>> VANILLA_MODEL = new ThreadLocal<>();

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD")
    )
    @SuppressWarnings("unchecked")
    private void vansqmod$rareBabyChickenModel(
            T entity,
            float entityYaw,
            float partialTicks,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        if (entity instanceof Chicken chicken && chicken.isBaby() && RareChickenVariants.isRare(chicken)) {
            VANILLA_MODEL.set(this.model);
            this.model = (M) RareChickenModels.INSTANCE.getBaby();
        }
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN")
    )
    @SuppressWarnings("unchecked")
    private void vansqmod$restoreChickenModel(
            T entity,
            float entityYaw,
            float partialTicks,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        EntityModel<?> previous = VANILLA_MODEL.get();
        if (previous != null) {
            this.model = (M) previous;
            VANILLA_MODEL.remove();
        }
    }
}
