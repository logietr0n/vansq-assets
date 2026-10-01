package com.vansqmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vansqmod.entity.BoulderingZombie;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neoforged.fml.ModList;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BoulderingZombieRenderer extends GeoEntityRenderer<BoulderingZombie> {

    public BoulderingZombieRenderer(EntityRendererProvider.Context context) {
        super(context, new BoulderingZombieModel());
        this.shadowRadius = 0.5F;
        this.addRenderLayer(new BoulderingZombieArmorLayer(this));
        this.addRenderLayer(new BoulderingZombieItemLayer(this));
        if (ModList.get().isLoaded("enchantwithmob")) {
            this.addRenderLayer(new BoulderingZombieEnchantedEyesLayer(this));
        }
    }

    @Override
    public void render(
            BoulderingZombie entity,
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
