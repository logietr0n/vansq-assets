package com.vansqmod.registry;

import com.vansqmod.VansqMod;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = VansqMod.MODID)
public class ModCreativeTabs {

    @SubscribeEvent
    public static void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {

        // Materials tab
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {

            event.insertAfter(
                    Items.GOLD_INGOT.getDefaultInstance(),
                    ModItems.ROSE_GOLD_INGOT.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.insertAfter(
                    Items.GOLD_NUGGET.getDefaultInstance(),
                    ModItems.ROSE_GOLD_NUGGET.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.insertAfter(
                    Items.RAW_GOLD.getDefaultInstance(),
                    ModItems.RAW_ROSE_GOLD.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.insertAfter(
                    Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE.getDefaultInstance(),
                    ModItems.DEEPSLATE_UPGRADE_SMITHING_TEMPLATE.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.insertAfter(
                    Items.AMETHYST_SHARD.getDefaultInstance(),
                    ModItems.RHODOHEART_SHARD.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        // Building blocks (anchors differ by MC version; raw ore blocks are not always on this tab)
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            var visibility = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
            ItemStack rawRoseGoldBlock = ModBlocks.RAW_ROSE_GOLD_BLOCK_ITEM.get().getDefaultInstance();
            ItemStack roseGoldBlock = ModBlocks.ROSE_GOLD_BLOCK_ITEM.get().getDefaultInstance();
            if (!tryInsertAfter(event, Items.RAW_GOLD_BLOCK.getDefaultInstance(), rawRoseGoldBlock, visibility)) {
                tryInsertAfter(event, Items.GOLD_BLOCK.getDefaultInstance(), rawRoseGoldBlock, visibility);
            }
            if (!tryInsertAfter(event, rawRoseGoldBlock, roseGoldBlock, visibility)) {
                tryInsertAfter(event, Items.GOLD_BLOCK.getDefaultInstance(), roseGoldBlock, visibility);
            }
            if (!tryInsertAfter(event, Items.CUT_SANDSTONE.getDefaultInstance(),
                    ModBlocks.GILDED_ANCIENT_SANDSTONE_ITEM.get().getDefaultInstance(), visibility)) {
                tryInsertAfter(event, Items.SANDSTONE.getDefaultInstance(),
                        ModBlocks.GILDED_ANCIENT_SANDSTONE_ITEM.get().getDefaultInstance(), visibility);
            }
            event.insertAfter(
                    Items.AMETHYST_BLOCK.getDefaultInstance(),
                    ModBlocks.RHODOHEART_BLOCK_ITEM.get().getDefaultInstance(),
                    visibility
            );
            if (!tryInsertAfter(event, Items.PEARLESCENT_FROGLIGHT.getDefaultInstance(),
                    ModBlocks.RHODOHEART_LAMP_ITEM.get().getDefaultInstance(), visibility)) {
                tryInsertAfter(event, ModBlocks.RHODOHEART_BLOCK_ITEM.get().getDefaultInstance(),
                        ModBlocks.RHODOHEART_LAMP_ITEM.get().getDefaultInstance(), visibility);
            }
        }

        // Natural blocks — rhodoheart crystals and moss carpet
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            var visibility = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
            event.insertAfter(
                    Items.AMETHYST_CLUSTER.getDefaultInstance(),
                    ModBlocks.BUDDING_RHODOHEART_ITEM.get().getDefaultInstance(),
                    visibility
            );
            event.insertAfter(
                    ModBlocks.BUDDING_RHODOHEART_ITEM.get().getDefaultInstance(),
                    ModBlocks.SMALL_RHODOHEART_BUD_ITEM.get().getDefaultInstance(),
                    visibility
            );
            event.insertAfter(
                    ModBlocks.SMALL_RHODOHEART_BUD_ITEM.get().getDefaultInstance(),
                    ModBlocks.MEDIUM_RHODOHEART_BUD_ITEM.get().getDefaultInstance(),
                    visibility
            );
            event.insertAfter(
                    ModBlocks.MEDIUM_RHODOHEART_BUD_ITEM.get().getDefaultInstance(),
                    ModBlocks.LARGE_RHODOHEART_BUD_ITEM.get().getDefaultInstance(),
                    visibility
            );
            event.insertAfter(
                    ModBlocks.LARGE_RHODOHEART_BUD_ITEM.get().getDefaultInstance(),
                    ModBlocks.RHODOHEART_CLUSTER_ITEM.get().getDefaultInstance(),
                    visibility
            );
            event.insertAfter(
                    ModBlocks.RHODOHEART_CLUSTER_ITEM.get().getDefaultInstance(),
                    ModBlocks.GLINTED_RHODOHEART_CLUSTER_ITEM.get().getDefaultInstance(),
                    visibility
            );
            event.insertAfter(
                    Items.MOSS_CARPET.getDefaultInstance(),
                    ModBlocks.LICHEN_MOSS_CARPET_ITEM.get().getDefaultInstance(),
                    visibility
            );
        }

        // Tools tab
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.insertAfter(
                    Items.GOLDEN_HOE.getDefaultInstance(),
                    ModItems.ROSE_GOLD_PICKAXE.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.insertAfter(
                    ModItems.ROSE_GOLD_PICKAXE.get().getDefaultInstance(),
                    ModItems.ROSE_GOLD_AXE.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.insertAfter(
                    ModItems.ROSE_GOLD_AXE.get().getDefaultInstance(),
                    ModItems.ROSE_GOLD_SHOVEL.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.insertAfter(
                    ModItems.ROSE_GOLD_SHOVEL.get().getDefaultInstance(),
                    ModItems.ROSE_GOLD_HOE.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }

        // Combat tab
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.insertAfter(
                    Items.GOLDEN_BOOTS.getDefaultInstance(),
                    ModItems.ROSE_GOLD_HELMET.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.insertAfter(
                    ModItems.ROSE_GOLD_HELMET.get().getDefaultInstance(),
                    ModItems.ROSE_GOLD_CHESTPLATE.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.insertAfter(
                    ModItems.ROSE_GOLD_CHESTPLATE.get().getDefaultInstance(),
                    ModItems.ROSE_GOLD_LEGGINGS.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.insertAfter(
                    ModItems.ROSE_GOLD_LEGGINGS.get().getDefaultInstance(),
                    ModItems.ROSE_GOLD_BOOTS.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            insertCopperKnifeCombat(event);

            event.insertAfter(
                    Items.GOLDEN_SWORD.getDefaultInstance(),
                    ModItems.ROSE_GOLD_SWORD.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.insertAfter(
                    ModItems.ROSE_GOLD_SWORD.get().getDefaultInstance(),
                    ModItems.ROSE_GOLD_KNIFE.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            event.insertAfter(
                    ModItems.ROSE_GOLD_KNIFE.get().getDefaultInstance(),
                    ModItems.SCYTHE.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
            ItemStack goldSpear = new ItemStack(
                    BuiltInRegistries.ITEM.get(
                            ResourceLocation.withDefaultNamespace("golden_spear")
                    )
            );

            ItemStack roseGoldSpear = new ItemStack(
                    BuiltInRegistries.ITEM.get(
                            ResourceLocation.withDefaultNamespace("rose_gold_spear")
                    )
            );

            ItemStack silverSpear = new ItemStack(
                    BuiltInRegistries.ITEM.get(
                            ResourceLocation.withDefaultNamespace("silver_spear")
                    )
            );

            ItemStack necromiumSpear = new ItemStack(
                    BuiltInRegistries.ITEM.get(
                            ResourceLocation.withDefaultNamespace("necromium_spear")
                    )
            );

            event.insertAfter(
                    goldSpear,
                    roseGoldSpear,
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );

            event.insertAfter(
                    roseGoldSpear,
                    silverSpear,
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );

            event.insertAfter(
                    silverSpear,
                    necromiumSpear,
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }
    }

    private static boolean tryInsertAfter(
            BuildCreativeModeTabContentsEvent event,
            ItemStack anchor,
            ItemStack stack,
            CreativeModeTab.TabVisibility visibility
    ) {
        try {
            event.insertAfter(anchor, stack, visibility);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    /** Place copper knife after {@code minecraft:copper_sword}, else CC's copper sword, else stone sword. */
    private static void insertCopperKnifeCombat(BuildCreativeModeTabContentsEvent event) {
        ItemStack knife = ModItems.COPPER_KNIFE.get().getDefaultInstance();
        var visibility = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
        for (ResourceLocation id : List.of(
                ResourceLocation.withDefaultNamespace("copper_sword"),
                ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "copper_sword"))) {
            var holder = BuiltInRegistries.ITEM.getOptional(id);
            if (holder.isPresent()) {
                event.insertAfter(new ItemStack(holder.get()), knife, visibility);
                return;
            }
        }
        event.insertAfter(Items.STONE_SWORD.getDefaultInstance(), knife, visibility);
    }
}
