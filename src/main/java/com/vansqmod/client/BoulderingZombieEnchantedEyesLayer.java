package com.vansqmod.client;

import baguchi.enchantwithmob.EnchantConfig;
import baguchi.enchantwithmob.api.IEnchantCap;
import baguchi.enchantwithmob.api.MobEnchantEye;
import baguchi.enchantwithmob.client.render.layer.EnchantedEyesLayer;
import baguchi.enchantwithmob.data.resources.registries.MobEnchantEyes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vansqmod.entity.BoulderingZombie;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class BoulderingZombieEnchantedEyesLayer extends GeoRenderLayer<BoulderingZombie> {

    public static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "enchantwithmob", "textures/entity/enchant_eye/enchanted_zombie_eyes.png");

    public BoulderingZombieEnchantedEyesLayer(GeoRenderer<BoulderingZombie> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            BoulderingZombie zombie,
            BakedGeoModel bakedModel,
            @Nullable RenderType renderType,
            MultiBufferSource bufferSource,
            @Nullable VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        if (!(zombie instanceof IEnchantCap cap) || !cap.getEnchantCap().hasEnchant()) {
            return;
        }
        if (EnchantConfig.CLIENT.disableEyeRender.get()) {
            return;
        }

        ResourceLocation texture = MobEnchantEyes.getEyeVariant(zombie.registryAccess(), zombie.getType())
                .map(MobEnchantEye::texture)
                .orElse(DEFAULT_TEXTURE);
        RenderType eyes = EnchantedEyesLayer.enchantedEyes(texture);
        getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                zombie,
                eyes,
                bufferSource.getBuffer(eyes),
                partialTick,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0xFFFFFFFF
        );
    }
}
