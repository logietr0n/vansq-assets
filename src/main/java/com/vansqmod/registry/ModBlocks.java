package com.vansqmod.registry;

import com.vansqmod.VansqMod;
import com.vansqmod.block.BuddingRhodoheartBlock;
import com.vansqmod.block.GildedAncientSandstone;
import com.vansqmod.block.GlintedRhodoheartClusterBlock;
import com.vansqmod.block.LichenMossCarpetBlock;
import com.vansqmod.block.RhodoheartBlock;
import com.vansqmod.block.RhodoheartPollinatedClusterBlock;
import com.vansqmod.compat.RhodoheartSoundTypes;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.MapColor;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(VansqMod.MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(VansqMod.MODID);

    private static BlockBehaviour.Properties rhodoheartCrystalBlockProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PINK)
                .strength(1.5F)
                .sound(RhodoheartSoundTypes.block())
                .requiresCorrectToolForDrops()
                .noOcclusion();
    }

    private static BlockBehaviour.Properties rhodoheartCrystalClusterProperties() {
        return rhodoheartCrystalBlockProperties().sound(RhodoheartSoundTypes.cluster());
    }

    private static BlockBehaviour.Properties buddingRhodoheartProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PINK)
                .strength(1.5F, 1.5F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .randomTicks();
    }

    // Gilded Ancient Sandstone
    public static final DeferredBlock<Block> GILDED_ANCIENT_SANDSTONE =
            BLOCKS.register("gilded_ancient_sandstone",
                    () -> new GildedAncientSandstone(
                            BlockBehaviour.Properties.of()
                                    .strength(2.0f, 6f)
                                    .requiresCorrectToolForDrops()
                                    .sound(SoundType.NETHER_GOLD_ORE)
                    ));

    public static final DeferredItem<BlockItem> GILDED_ANCIENT_SANDSTONE_ITEM =
            ITEMS.registerSimpleBlockItem("gilded_ancient_sandstone", GILDED_ANCIENT_SANDSTONE);

    // Raw Rose Gold Block
    public static final DeferredBlock<Block> RAW_ROSE_GOLD_BLOCK =
            BLOCKS.register("raw_rose_gold_block",
                    () -> new Block(BlockBehaviour.Properties.of()
                            .strength(5.0f, 6.0f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    public static final DeferredItem<BlockItem> RAW_ROSE_GOLD_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("raw_rose_gold_block", RAW_ROSE_GOLD_BLOCK);

    // Rose Gold Block
    public static final DeferredBlock<Block> ROSE_GOLD_BLOCK =
            BLOCKS.register("rose_gold_block",
                    () -> new Block(BlockBehaviour.Properties.of()
                            .strength(3.0f, 6.0f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));

    public static final DeferredItem<BlockItem> ROSE_GOLD_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("rose_gold_block", ROSE_GOLD_BLOCK);

    // Lichen Moss Carpet (Galosphere-style lit/stepped behavior)
    public static final DeferredBlock<Block> LICHEN_MOSS_CARPET =
            BLOCKS.register("lichen_moss_carpet",
                    () -> new LichenMossCarpetBlock(BlockBehaviour.Properties.of()
                            .strength(0.1f)
                            .sound(SoundType.MOSS_CARPET)
                            .noOcclusion()
                            .lightLevel(state -> state.getValue(LichenMossCarpetBlock.LIT) ? 12 : 0)));

    public static final DeferredItem<BlockItem> LICHEN_MOSS_CARPET_ITEM =
            ITEMS.registerSimpleBlockItem("lichen_moss_carpet", LICHEN_MOSS_CARPET);

    // Rhodoheart crystals
    public static final DeferredBlock<RhodoheartBlock> RHODOHEART_BLOCK =
            BLOCKS.register("rhodoheart_block",
                    () -> new RhodoheartBlock(rhodoheartCrystalBlockProperties()));

    public static final DeferredItem<BlockItem> RHODOHEART_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("rhodoheart_block", RHODOHEART_BLOCK);

    public static final DeferredBlock<Block> RHODOHEART_LAMP =
            BLOCKS.register("rhodoheart_lamp",
                    () -> new Block(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PINK)
                            .lightLevel(state -> 15)
                            .strength(0.3F)
                            .sound(RhodoheartSoundTypes.block())));

    public static final DeferredItem<BlockItem> RHODOHEART_LAMP_ITEM =
            ITEMS.registerSimpleBlockItem("rhodoheart_lamp", RHODOHEART_LAMP);

    public static final DeferredBlock<BuddingRhodoheartBlock> BUDDING_RHODOHEART =
            BLOCKS.register("budding_rhodoheart",
                    () -> new BuddingRhodoheartBlock(buddingRhodoheartProperties()));

    public static final DeferredItem<BlockItem> BUDDING_RHODOHEART_ITEM =
            ITEMS.registerSimpleBlockItem("budding_rhodoheart", BUDDING_RHODOHEART);

    public static final DeferredBlock<Block> SMALL_RHODOHEART_BUD =
            BLOCKS.register("small_rhodoheart_bud",
                    () -> new AmethystClusterBlock(3, 4,
                            rhodoheartCrystalClusterProperties().randomTicks().lightLevel(state -> 1)));

    public static final DeferredItem<BlockItem> SMALL_RHODOHEART_BUD_ITEM =
            ITEMS.registerSimpleBlockItem("small_rhodoheart_bud", SMALL_RHODOHEART_BUD);

    public static final DeferredBlock<Block> MEDIUM_RHODOHEART_BUD =
            BLOCKS.register("medium_rhodoheart_bud",
                    () -> new AmethystClusterBlock(4, 3,
                            rhodoheartCrystalClusterProperties().randomTicks().lightLevel(state -> 2)));

    public static final DeferredItem<BlockItem> MEDIUM_RHODOHEART_BUD_ITEM =
            ITEMS.registerSimpleBlockItem("medium_rhodoheart_bud", MEDIUM_RHODOHEART_BUD);

    public static final DeferredBlock<Block> LARGE_RHODOHEART_BUD =
            BLOCKS.register("large_rhodoheart_bud",
                    () -> new AmethystClusterBlock(5, 3,
                            rhodoheartCrystalClusterProperties().randomTicks().lightLevel(state -> 4)));

    public static final DeferredItem<BlockItem> LARGE_RHODOHEART_BUD_ITEM =
            ITEMS.registerSimpleBlockItem("large_rhodoheart_bud", LARGE_RHODOHEART_BUD);

    public static final DeferredBlock<RhodoheartPollinatedClusterBlock> RHODOHEART_CLUSTER =
            BLOCKS.register("rhodoheart_cluster",
                    () -> new RhodoheartPollinatedClusterBlock(
                            ModParticleTypes.RHODOHEART_RAIN,
                            rhodoheartCrystalClusterProperties().randomTicks().lightLevel(state -> 5)));

    public static final DeferredItem<BlockItem> RHODOHEART_CLUSTER_ITEM =
            ITEMS.registerSimpleBlockItem("rhodoheart_cluster", RHODOHEART_CLUSTER);

    public static final DeferredBlock<GlintedRhodoheartClusterBlock> GLINTED_RHODOHEART_CLUSTER =
            BLOCKS.register("glinted_rhodoheart_cluster",
                    () -> new GlintedRhodoheartClusterBlock(
                            rhodoheartCrystalClusterProperties().randomTicks().lightLevel(state -> 7)));

    public static final DeferredItem<BlockItem> GLINTED_RHODOHEART_CLUSTER_ITEM =
            ITEMS.registerSimpleBlockItem("glinted_rhodoheart_cluster", GLINTED_RHODOHEART_CLUSTER);
}
