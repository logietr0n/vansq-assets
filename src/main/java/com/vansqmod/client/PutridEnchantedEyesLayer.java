package com.vansqmod.client;

import baguchi.enchantwithmob.EnchantConfig;
import baguchi.enchantwithmob.api.IEnchantCap;
import baguchi.enchantwithmob.api.MobEnchantEye;
import baguchi.enchantwithmob.client.render.layer.EnchantedEyesLayer;
import baguchi.enchantwithmob.data.resources.registries.MobEnchantEyes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vansqmod.VansqMod;
import com.vansqmod.entity.Putrid;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * EnchantWithMob's {@code EnchantedEyesLayer} is the magenta/purple emissive overlay
 * (not the enchanted glint/{@code EnchantLayer}). It looks up a texture from the
 * {@code enchantwithmob:mob_enchant_eye} datapack registry and redraws the model
 * with {@link EnchantedEyesLayer#enchantedEyes} (energy-swirl shader + translucent).
 *
 * <p>That layer is added in {@code EntityRenderersEvent.AddLayers} only to
 * {@code LivingEntityRenderer}. GeckoLib 4.9's {@code GeoEntityRenderer} extends
 * {@code EntityRenderer} directly, so Putrid never gets it. This geo layer does
 * the same redraw against the baked putrid mesh.</p>
 */
public class PutridEnchantedEyesLayer extends GeoRenderLayer<Putrid> {

    public static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            VansqMod.MODID, "textures/entity/enchant_eye/enchanted_putrid_eyes.png");

    public PutridEnchantedEyesLayer(GeoRenderer<Putrid> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            Putrid putrid,
            BakedGeoModel bakedModel,
            @Nullable RenderType renderType,
            MultiBufferSource bufferSource,
            @Nullable VertexConsumer buffer,
            float partialTick,
            int packedLight,
            int packedOverlay
    ) {
        if (!(putrid instanceof IEnchantCap cap) || !cap.getEnchantCap().hasEnchant()) {
            return;
        }
        if (EnchantConfig.CLIENT.disableEyeRender.get()) {
            return;
        }

        ResourceLocation texture = MobEnchantEyes.getEyeVariant(putrid.registryAccess(), putrid.getType())
                .map(MobEnchantEye::texture)
                .orElse(DEFAULT_TEXTURE);
        RenderType eyes = EnchantedEyesLayer.enchantedEyes(texture);
        getRenderer().reRender(
                bakedModel,
                poseStack,
                bufferSource,
                putrid,
                eyes,
                bufferSource.getBuffer(eyes),
                partialTick,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0xFFFFFFFF
        );
    }
}
