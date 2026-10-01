package com.vansqmod.compat;

import com.teamabnormals.incubation.common.block.BirdNestBlock;
import com.teamabnormals.incubation.common.block.EmptyNestBlock;
import com.teamabnormals.incubation.core.registry.IncubationBlocks;
import com.vansqmod.VansqMod;
import com.vansqmod.registry.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Incubation nests and crate for the rare chicken egg, matching vbincubationcompat's
 * blue/brown egg wiring. No worldgen nests — the variant is breeding-only.
 */
public final class RareChickenIncubation {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(VansqMod.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VansqMod.MODID);

    public static final DeferredBlock<Block> RARE_EGG_CRATE = BLOCKS.register(
            "rare_egg_crate",
            () -> new Block(crateProperties()));
    public static final DeferredItem<BlockItem> RARE_EGG_CRATE_ITEM = ITEMS.register(
            "rare_egg_crate",
            () -> new BlockItem(RARE_EGG_CRATE.get(), new Item.Properties()));

    public static final DeferredBlock<Block> TWIG_RARE_NEST = BLOCKS.register(
            "twig_rare_nest",
            () -> new BirdNestBlock(
                    ModItems.RARE_EGG,
                    (EmptyNestBlock) IncubationBlocks.TWIG_NEST.get(),
                    nestProperties()));
    public static final DeferredBlock<Block> HAY_RARE_NEST = BLOCKS.register(
            "hay_rare_nest",
            () -> new BirdNestBlock(
                    ModItems.RARE_EGG,
                    (EmptyNestBlock) IncubationBlocks.HAY_NEST.get(),
                    nestProperties()));

    private RareChickenIncubation() {
    }

    public static void init(IEventBus bus) {
        if (!ModList.get().isLoaded("incubation") || !ModList.get().isLoaded("vanillabackport")) {
            return;
        }
        BLOCKS.register(bus);
        ITEMS.register(bus);
        bus.addListener(RareChickenIncubation::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            EmptyNestBlock twig = (EmptyNestBlock) IncubationBlocks.TWIG_NEST.get();
            EmptyNestBlock hay = (EmptyNestBlock) IncubationBlocks.HAY_NEST.get();
            twig.addNest(ModItems.RARE_EGG, TWIG_RARE_NEST.get());
            hay.addNest(ModItems.RARE_EGG, HAY_RARE_NEST.get());
        });
    }

    private static BlockBehaviour.Properties nestProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_YELLOW)
                .strength(0.5F)
                .sound(SoundType.GRASS);
    }

    private static BlockBehaviour.Properties crateProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_WHITE)
                .strength(2.0F)
                .sound(SoundType.WOOD);
    }
}
