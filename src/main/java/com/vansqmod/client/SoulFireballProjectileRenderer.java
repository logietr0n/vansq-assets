package com.vansqmod.client;

import com.vansqmod.VansqMod;
import com.vansqmod.entity.SoulFireballProjectile;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SoulFireballProjectileRenderer extends TumblingCubeProjectileRenderer<SoulFireballProjectile> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "textures/entity/projectiles/soul_fireball.png");
    private static final ResourceLocation OVERLAY =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "textures/entity/projectiles/soul_fireball_overlay.png");

    public SoulFireballProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, TEXTURE, OVERLAY, true);
    }
}
