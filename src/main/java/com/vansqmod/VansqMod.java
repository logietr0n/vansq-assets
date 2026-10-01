package com.vansqmod;

import com.vansqmod.compat.EssentialLoaderUpdates;
import com.vansqmod.compat.PackItemComponents;
import com.vansqmod.entity.RareChickenVariants;
import com.vansqmod.config.BlockedEntityConfig;
import com.vansqmod.config.ObliteratorItemConfig;
import com.vansqmod.config.VansqModClientConfigs;
import com.vansqmod.entity.IceSpiderSpawns;
import com.vansqmod.entity.OceanSpiderSpawns;
import com.vansqmod.entity.PutridSwampSpawns;
import com.vansqmod.integration.backpacks.BackpackCuriosIntegration;
import com.vansqmod.integration.beltborne.BeltborneLanternCuriosIntegration;
import com.vansqmod.integration.missionaryhat.MissionaryHatCuriosIntegration;
import com.vansqmod.integration.tetherpotion.TetherPotionCuriosIntegration;
import com.vansqmod.integration.toolbelt.ToolbeltCuriosIntegration;
import com.vansqmod.item.CrownCuriosIntegration;
import com.vansqmod.registry.ModAttachments;
import com.vansqmod.registry.ModAttributes;
import com.vansqmod.registry.ModBiomeModifiers;
import com.vansqmod.registry.ModBlocks;
import com.vansqmod.registry.ModEntityTypes;
import com.vansqmod.registry.ModFeatures;
import com.vansqmod.registry.ModItems;
import com.vansqmod.registry.ModParticleTypes;
import com.vansqmod.registry.ModPlacementModifiers;
import com.vansqmod.registry.ModSoundEvents;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(VansqMod.MODID)
public class VansqMod {

    public static final String MODID = "vansqmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public VansqMod(IEventBus bus) {

        // Register blocks
        ModBlocks.BLOCKS.register(bus);
        ModBlocks.ITEMS.register(bus);

        // Register features
        ModFeatures.FEATURES.register(bus);
        ModPlacementModifiers.PLACEMENT_MODIFIER_TYPES.register(bus);
        ModBiomeModifiers.SERIALIZERS.register(bus);

        ModParticleTypes.PARTICLE_TYPES.register(bus);

        ModEntityTypes.ENTITY_TYPES.register(bus);
        ModSoundEvents.SOUND_EVENTS.register(bus);
        ModAttachments.ATTACHMENT_TYPES.register(bus);

        // Register items
        ModAttributes.ATTRIBUTES.register(bus);
        ModItems.ITEMS.register(bus);
        if (ModList.get().isLoaded("vanillabackport")) {
            RareChickenVariants.bootstrap();
        }
        if (ModList.get().isLoaded("incubation") && ModList.get().isLoaded("vanillabackport")) {
            try {
                Class.forName("com.vansqmod.compat.RareChickenIncubation")
                        .getMethod("init", IEventBus.class)
                        .invoke(null, bus);
            } catch (ReflectiveOperationException e) {
                LOGGER.warn("Failed to initialize rare chicken Incubation compat", e);
            }
        }
        bus.addListener(PackItemComponents::onModifyDefaultComponents);

        if (ModList.get().isLoaded("spider_overhaul")) {
            bus.addListener(EventPriority.LOWEST, OceanSpiderSpawns::registerSpawnPlacements);
            bus.addListener(EventPriority.LOWEST, IceSpiderSpawns::registerSpawnPlacements);
        }
        if (ModList.get().isLoaded("variantsandventures")) {
            bus.addListener(PutridSwampSpawns::onCommonSetup);
        }

        BackpackCuriosIntegration.init(bus);
        ToolbeltCuriosIntegration.init(bus);
        BeltborneLanternCuriosIntegration.init(bus);
        MissionaryHatCuriosIntegration.init(bus);
        TetherPotionCuriosIntegration.init(bus);
        CrownCuriosIntegration.init(bus);

        BlockedEntityConfig.load();
        ObliteratorItemConfig.load();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            EssentialLoaderUpdates.suppressPrompts();
            bus.addListener((FMLClientSetupEvent event) -> VansqModClientConfigs.loadAll());
        }
    }
}