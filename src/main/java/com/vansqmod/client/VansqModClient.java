package com.vansqmod.client;

import com.vansqmod.VansqMod;
import com.vansqmod.client.particle.RhodoheartRainParticle;
import com.vansqmod.client.particle.ScytheSweepParticle;
import com.vansqmod.registry.ModParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public class VansqModClient {

    private static final ResourceLocation ATLAS_INSTRUMENT_HUD =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "atlas_instruments");
    private static final ResourceLocation ATLAS_MINIMAP_HINT =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "atlas_minimap_hint");

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.RHODOHEART_RAIN.get(), RhodoheartRainParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SCYTHE_SWEEP.get(), ScytheSweepParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        if (!ModList.get().isLoaded("map_atlases")) {
            return;
        }
        // Same band as Map Atlases HUD; we only draw when that HUD early-outs (no active atlas).
        event.registerBelow(VanillaGuiLayers.DEBUG_OVERLAY, ATLAS_INSTRUMENT_HUD, new MapAtlasesInstrumentHud());
        event.registerBelow(VanillaGuiLayers.DEBUG_OVERLAY, ATLAS_MINIMAP_HINT, new AtlasMinimapHintHud());
    }
}
