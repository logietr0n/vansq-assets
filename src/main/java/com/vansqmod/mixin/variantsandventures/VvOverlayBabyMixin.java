package com.vansqmod.mixin.variantsandventures;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vansqmod.client.DrownedBabyModels;
import com.vansqmod.client.VvDrownedBabyOverlay;
import com.vansqmod.client.VvZombieVariantSkins;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

/**
 * Gelid/Thicket clothes are a drowned-style outer layer. Tiny Takeover only
 * swaps that mesh for vanilla drowned, so babies otherwise keep the adult
 * overlay on the baby body. Use the drowned-baby outer mesh and V&amp;V baby sheets.
 */
@Mixin(targets = {
        "com.faboslav.variantsandventures.common.client.render.entity.feature.GelidOverlayFeatureRenderer",
        "com.faboslav.variantsandventures.common.client.render.entity.feature.ThicketOverlayFeatureRenderer"
})
public abstract class VvOverlayBabyMixin<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    @Unique
    private EntityModel<T> vansqmod$adultOverlay;

    @Unique
    private HumanoidModel<T> vansqmod$babyOverlay;

    private VvOverlayBabyMixin(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    @SuppressWarnings("unchecked")
    private void vansqmod$initBabyOuter(RenderLayerParent<T, M> parent, EntityModelSet modelSet, CallbackInfo ci) {
        try {
            Field field = this.getClass().getDeclaredField("model");
            field.setAccessible(true);
            this.vansqmod$adultOverlay = (EntityModel<T>) field.get(this);
        } catch (ReflectiveOperationException ignored) {
        }
        try {
            this.vansqmod$babyOverlay = new HumanoidModel<>(modelSet.bakeLayer(DrownedBabyModels.OUTER));
        } catch (RuntimeException ignored) {
            this.vansqmod$babyOverlay = null;
        }
    }

    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    @SuppressWarnings("unchecked")
    private void vansqmod$renderOverlay(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            Entity entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        if (!(entity instanceof LivingEntity living) || this.vansqmod$adultOverlay == null) {
            return;
        }
        T mob = (T) living;
        ResourceLocation texture = VvZombieVariantSkins.overlay(mob, vansqmod$vvOverlay(mob));
        ci.cancel();
        if (mob.isBaby()
                && this.vansqmod$babyOverlay != null
                && VvDrownedBabyOverlay.isBabyMesh(this.getParentModel())) {
            VvDrownedBabyOverlay.render(
                    this.getParentModel(),
                    this.vansqmod$babyOverlay,
                    texture,
                    poseStack,
                    buffer,
                    packedLight,
                    mob,
                    partialTick
            );
            return;
        }
        RenderLayer.coloredCutoutModelCopyLayerRender(
                this.getParentModel(),
                this.vansqmod$adultOverlay,
                texture,
                poseStack,
                buffer,
                packedLight,
                mob,
                limbSwing,
                limbSwingAmount,
                ageInTicks,
                netHeadYaw,
                headPitch,
                partialTick,
                -1
        );
    }

    @Unique
    private static ResourceLocation vansqmod$vvOverlay(LivingEntity entity) {
        String path = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        return ResourceLocation.fromNamespaceAndPath(
                "variantsandventures",
                "textures/entity/" + path + "/" + path + "_overlay.png"
        );
    }
}
