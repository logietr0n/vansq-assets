package com.vansqmod.client;

import com.vansqmod.VansqMod;
import com.vansqmod.client.particle.FallingLeafParticle;
import com.vansqmod.client.particle.IceFlakeParticle;
import com.vansqmod.client.particle.RhodoheartRainParticle;
import com.vansqmod.client.particle.SoulFireballFlameParticle;
import com.vansqmod.integration.missionaryhat.MissionaryHatEquipment;
import com.vansqmod.integration.tetherpotion.TetherPotionEquipment;
import com.vansqmod.registry.ModEntityTypes;
import com.vansqmod.registry.ModItems;
import com.vansqmod.registry.ModParticleTypes;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public class VansqModClient {

    private static final ResourceLocation ATLAS_INSTRUMENT_HUD =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "atlas_instruments");
    private static final ResourceLocation ATLAS_MINIMAP_HINT =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "atlas_minimap_hint");

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(RareChickenModels.INSTANCE);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        if (!ModList.get().isLoaded("variantsandventures")) {
            return;
        }
        event.registerLayerDefinition(
                DrownedBabyModels.BODY,
                () -> DrownedBabyModels.createBodyLayer(CubeDeformation.NONE)
        );
        event.registerLayerDefinition(
                DrownedBabyModels.OUTER,
                () -> DrownedBabyModels.createBodyLayer(new CubeDeformation(0.25F))
        );
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.MELLOWED.get(), MellowedRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.PUTRID.get(), PutridRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BOULDERING_ZOMBIE.get(), BoulderingZombieRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ICICLE.get(), IcicleProjectileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ICE_SNOWBALL.get(), IceSnowballProjectileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SOUL_FIRE_CHARGE.get(), SoulFireballProjectileRenderer::new);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerCurioRenderersEarly(EntityRenderersEvent.AddLayers event) {
        if (!ModList.get().isLoaded("curios")) {
            return;
        }
        CuriosRendererRegistry.register(Items.SPYGLASS, SpyglassCurioRenderer::new);
        CharmTotemCuriosClient.registerCharmRenderers();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerCurioRenderersLate(EntityRenderersEvent.AddLayers event) {
        if (ModList.get().isLoaded("curios")) {
            CuriosRendererRegistry.register(Items.SPYGLASS, SpyglassCurioRenderer::new);
            CharmTotemCuriosClient.registerCharmRenderers();
            registerCrownRenderers();
        }
        if (ModList.get().isLoaded("geckolib") && ModList.get().isLoaded("netherexp")) {
            for (EntityType<?> type : event.getEntityTypes()) {
                EntityRenderer<?> renderer = event.getRenderer(type);
                if (renderer instanceof GeoEntityRenderer<?> geo) {
                    addJneBurningGeoLayer(geo);
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addJneBurningGeoLayer(GeoEntityRenderer renderer) {
        renderer.addRenderLayer(new JneBurningGeoLayer(renderer));
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ReactiveMusicMuteCompat::ensureBossMusicMuteEntry);
        event.enqueueWork(() -> {
            registerShieldBlocking(ModItems.PALLADIUM_BULWARK.get());
            registerShieldBlocking(ModItems.SILVER_TARGE.get());
            registerShieldBlocking(ModItems.CHAOS_FORTRESS.get());
            registerShieldBlocking(ModItems.NECROMIUM_GUARD.get());
            if (ModList.get().isLoaded("born_in_chaos_v1") && ModList.get().isLoaded("curios")) {
                BuiltInRegistries.ITEM.getOptional(MissionaryHatEquipment.HAT_ITEM)
                        .ifPresent(item -> CuriosRendererRegistry.register(item, MissionaryHatCurioRenderer::new));
            }
            if (ModList.get().isLoaded("caverns_and_chasms") && ModList.get().isLoaded("curios")) {
                BuiltInRegistries.ITEM.forEach(item -> {
                    if (TetherPotionEquipment.isPotionItem(item)) {
                        CuriosRendererRegistry.register(item, TetherPotionCurioRenderer::new);
                    }
                });
            }
            if (ModList.get().isLoaded("curios")) {
                CuriosRendererRegistry.register(Items.SPYGLASS, SpyglassCurioRenderer::new);
                CharmTotemCuriosClient.registerCharmRenderers();
                registerCrownRenderers();
            }
        });
    }

    private static void registerCrownRenderers() {
        CuriosRendererRegistry.register(
                ModItems.GOLDEN_CROWN.get(),
                () -> new CrownCurioRenderer(CrownCurioRenderer.GOLDEN)
        );
        CuriosRendererRegistry.register(
                ModItems.SILVER_CROWN.get(),
                () -> new CrownCurioRenderer(CrownCurioRenderer.SILVER)
        );
    }

    private static void registerShieldBlocking(Item item) {
        ItemProperties.register(
                item,
                ResourceLocation.withDefaultNamespace("blocking"),
                (stack, level, entity, seed) ->
                        entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F
        );
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.RHODOHEART_RAIN.get(), RhodoheartRainParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.ASPEN_LEAVES.get(), FallingLeafParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.ROTTEN_LEAVES.get(), FallingLeafParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.ECHO_LEAVES.get(), FallingLeafParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.OPAL_LEAVES.get(), FallingLeafParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.BLUE_BLOSSOM_LEAVES.get(), FallingLeafParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.LAVENDER_BLOSSOM_LEAVES.get(), FallingLeafParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.ORANGE_BLOSSOM_LEAVES.get(), FallingLeafParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.RED_BLOSSOM_LEAVES.get(), FallingLeafParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.YELLOW_BLOSSOM_LEAVES.get(), FallingLeafParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.ICE_FLAKE.get(), IceFlakeParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SOUL_FIREBALL_TRAIL.get(), SoulFireballFlameParticle.Provider::new);
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
