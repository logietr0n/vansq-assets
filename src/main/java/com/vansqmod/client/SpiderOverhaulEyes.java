package com.vansqmod.client;

import com.vansqmod.VansqMod;
import net.minecraft.resources.ResourceLocation;

/**
 * Maps a Spider Overhaul body texture ({@code spider_overhaul:textures/model/...})
 * to the vansqmod glowing-eye overlay the player crops from a full-texture copy.
 */
public final class SpiderOverhaulEyes {

    private static final String MODEL_PREFIX = "textures/model/";
    private static final String PNG = ".png";

    private SpiderOverhaulEyes() {
    }

    public static ResourceLocation overlayTexture(ResourceLocation bodyTexture) {
        if (bodyTexture == null || !"spider_overhaul".equals(bodyTexture.getNamespace())) {
            return null;
        }
        String path = bodyTexture.getPath();
        if (!path.startsWith(MODEL_PREFIX) || !path.endsWith(PNG)) {
            return null;
        }
        String rest = path.substring(MODEL_PREFIX.length(), path.length() - PNG.length());
        return ResourceLocation.fromNamespaceAndPath(
                VansqMod.MODID,
                "textures/entity/spider_overhaul/" + rest + "_eyes.png");
    }
}
