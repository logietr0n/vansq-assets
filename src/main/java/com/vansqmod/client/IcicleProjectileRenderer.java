package com.vansqmod.client;

import com.vansqmod.entity.IcicleProjectile;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class IcicleProjectileRenderer extends ArrowRenderer<IcicleProjectile> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(com.vansqmod.VansqMod.MODID, "textures/entity/projectiles/icicle.png");

    public IcicleProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(IcicleProjectile entity) {
        return TEXTURE;
    }
}
