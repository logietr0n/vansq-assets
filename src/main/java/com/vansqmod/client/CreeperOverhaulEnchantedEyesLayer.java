package com.vansqmod.client;

import com.vansqmod.VansqMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import tech.thatgravyboat.creeperoverhaul.common.entity.custom.PufferfishCreeper;

/**
 * Ocean Creepers swap {@code ocean_1/2/3} geo and UVs with puff stage. Teal and brown
 * variants share that geo, so the same overlay is used for both skins.
 */
public class CreeperOverhaulEnchantedEyesLayer<T extends LivingEntity & GeoAnimatable> extends GeoEnchantedEyesLayer<T> {

    private static final ResourceLocation[] OCEAN_STAGE_EYES = {
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "textures/entity/enchant_eye/enchanted_ocean_creeper_1_eyes.png"),
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "textures/entity/enchant_eye/enchanted_ocean_creeper_2_eyes.png"),
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "textures/entity/enchant_eye/enchanted_ocean_creeper_3_eyes.png")
    };

    public CreeperOverhaulEnchantedEyesLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    @Nullable
    protected ResourceLocation resolveTexture(T entity) {
        if (entity instanceof PufferfishCreeper puffer) {
            int stage = Mth.clamp(puffer.getPuffId(), 1, 3);
            return OCEAN_STAGE_EYES[stage - 1];
        }
        return super.resolveTexture(entity);
    }
}
