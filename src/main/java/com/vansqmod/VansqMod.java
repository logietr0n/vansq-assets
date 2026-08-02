package com.vansqmod;

import com.vansqmod.config.VansqModClientConfigs;
import com.vansqmod.integration.backpacks.BackpackCuriosIntegration;
import com.vansqmod.integration.beltborne.BeltborneLanternCuriosIntegration;
import com.vansqmod.integration.toolbelt.ToolbeltCuriosIntegration;
import com.vansqmod.registry.ModBlocks;
import com.vansqmod.registry.ModFeatures;
import com.vansqmod.registry.ModItems;
import com.vansqmod.registry.ModParticleTypes;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
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

        ModParticleTypes.PARTICLE_TYPES.register(bus);

        // Register items
        ModItems.ITEMS.register(bus);

        BackpackCuriosIntegration.init(bus);
        ToolbeltCuriosIntegration.init(bus);
        BeltborneLanternCuriosIntegration.init(bus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            bus.addListener((FMLClientSetupEvent event) -> VansqModClientConfigs.loadAll());
        }
    }
}