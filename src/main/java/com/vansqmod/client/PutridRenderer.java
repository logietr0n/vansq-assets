package com.vansqmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vansqmod.entity.Putrid;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neoforged.fml.ModList;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PutridRenderer extends GeoEntityRenderer<Putrid> {

    public PutridRenderer(EntityRendererProvider.Context context) {
        super(context, new PutridModel());
        this.shadowRadius = 0.5F;
        // The _outer bones sample the hat/jacket/sleeve/pants overlay UVs baked into
        // putrid.png itself (same texture, same draw call) -- no layer needed for those.
        // Vanilla armor DOES need a layer: GeoEntityRenderer has no HumanoidArmorLayer
        // equivalent built in. See PutridArmorLayer's Javadoc.
        this.addRenderLayer(new PutridArmorLayer(this));
        this.addRenderLayer(new PutridItemLayer(this));
        // EnchantWithMob only attaches EnchantedEyesLayer to LivingEntityRenderer.
        // GeoEntityRenderer is a plain EntityRenderer, so Putrid needs its own overlay.
        if (ModList.get().isLoaded("enchantwithmob")) {
            this.addRenderLayer(new PutridEnchantedEyesLayer(this));
        }
    }

    @Override
    public void render(
            Putrid entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        this.shadowRadius = entity.isBaby() ? 0.25F : 0.5F;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
