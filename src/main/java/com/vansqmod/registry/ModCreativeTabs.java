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
            ItemStack permafrostPalladium = ModBlocks.PERMAFROST_PALLADIUM_ORE_ITEM.get().getDefaultInstance();
            ItemStack deepslatePalladium = new ItemStack(
                    BuiltInRegistries.ITEM.get(
                            ResourceLocation.fromNamespaceAndPath("galosphere", "deepslate_palladium_ore")
                    )
            );
            if (deepslatePalladium.isEmpty()
                    || !tryInsertAfter(event, deepslatePalladium, permafrostPalladium, visibility)) {
                tryInsertAfter(event, Items.DEEPSLATE_IRON_ORE.getDefaultInstance(), permafrostPalladium, visibility);
            }
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
            insertVansqShields(event);
        }

        insertCrowns(event);
        insertSoulFireCharge(event);
        insertMellowedSpawnEgg(event);
        insertPutridSpawnEgg(event);
        insertBoulderingZombieSpawnEgg(event);
        insertGoldenMaggot(event);
        insertTweed(event);
        insertRareChickenItems(event);
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

    /**
     * Place Soul Fire Charge immediately after vanilla Fire Charge on whichever
     * tab actually contains it (Combat in vanilla; some packs move it).
     */
    private static void insertSoulFireCharge(BuildCreativeModeTabContentsEvent event) {
        var visibility = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
        ItemStack soul = ModItems.SOUL_FIRE_CHARGE.get().getDefaultInstance();
        if (tryInsertAfter(event, Items.FIRE_CHARGE.getDefaultInstance(), soul, visibility)) {
            return;
        }
        if (event.getTabKey() != CreativeModeTabs.COMBAT) {
            return;
        }
        if (!tryInsertAfter(event, Items.WIND_CHARGE.getDefaultInstance(), soul, visibility)) {
            tryInsertAfter(event, Items.SNOWBALL.getDefaultInstance(), soul, visibility);
        }
    }

    /** Place both crowns after the Caverns & Chasms toolbelt, on whichever tab holds it. */
    private static void insertCrowns(BuildCreativeModeTabContentsEvent event) {
        var toolbelt = BuiltInRegistries.ITEM.getOptional(
                ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "toolbelt"));
        if (toolbelt.isEmpty()) {
            return;
        }
        var visibility = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
        ItemStack golden = ModItems.GOLDEN_CROWN.get().getDefaultInstance();
        ItemStack silver = ModItems.SILVER_CROWN.get().getDefaultInstance();
        if (!tryInsertAfter(event, new ItemStack(toolbelt.get()), golden, visibility)) {
            return;
        }
        if (!tryInsertAfter(event, golden, silver, visibility)) {
            event.accept(silver, visibility);
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

    private static void insertMellowedSpawnEgg(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.SPAWN_EGGS) {
            return;
        }
        var visibility = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
        ItemStack egg = ModItems.MELLOWED_SPAWN_EGG.get().getDefaultInstance();
        if (!tryInsertAfter(event, Items.BOGGED_SPAWN_EGG.getDefaultInstance(), egg, visibility)) {
            tryInsertAfter(event, Items.SKELETON_SPAWN_EGG.getDefaultInstance(), egg, visibility);
        }
    }

    private static void insertPutridSpawnEgg(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.SPAWN_EGGS) {
            return;
        }
        var visibility = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
        ItemStack egg = ModItems.PUTRID_SPAWN_EGG.get().getDefaultInstance();
        if (!tryInsertAfter(event, Items.DROWNED_SPAWN_EGG.getDefaultInstance(), egg, visibility)) {
            tryInsertAfter(event, Items.ZOMBIE_SPAWN_EGG.getDefaultInstance(), egg, visibility);
        }
    }

    private static void insertBoulderingZombieSpawnEgg(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.SPAWN_EGGS) {
            return;
        }
        var visibility = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
        ItemStack egg = ModItems.BOULDERING_ZOMBIE_SPAWN_EGG.get().getDefaultInstance();
        if (tryInsertAfter(event, ModItems.PUTRID_SPAWN_EGG.get().getDefaultInstance(), egg, visibility)) {
            return;
        }
        if (tryInsertAfter(event, Items.ZOMBIE_SPAWN_EGG.getDefaultInstance(), egg, visibility)) {
            return;
        }
        event.accept(egg, visibility);
    }

    private static void insertVansqShields(BuildCreativeModeTabContentsEvent event) {
        var visibility = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
        ItemStack after = BuiltInRegistries.ITEM.getOptional(
                ResourceLocation.fromNamespaceAndPath("piglinproliferation", "buckler"))
                .map(ItemStack::new)
                .orElseGet(Items.SHIELD::getDefaultInstance);
        for (ItemStack shield : List.of(
                ModItems.ROSE_GOLD_DOUBUCKLER.get().getDefaultInstance(),
                ModItems.PALLADIUM_BULWARK.get().getDefaultInstance(),
                ModItems.SILVER_TARGE.get().getDefaultInstance(),
                ModItems.CHAOS_FORTRESS.get().getDefaultInstance(),
                ModItems.NECROMIUM_GUARD.get().getDefaultInstance()
        )) {
            if (tryInsertAfter(event, after, shield, visibility)) {
                after = shield;
            } else {
                event.accept(shield, visibility);
                after = shield;
            }
        }
    }

    private static void insertTweed(BuildCreativeModeTabContentsEvent event) {
        var straw = BuiltInRegistries.ITEM.getOptional(
                ResourceLocation.fromNamespaceAndPath("farmersdelight", "straw"));
        if (straw.isEmpty()) {
            return;
        }
        tryInsertAfter(
                event,
                straw.get().getDefaultInstance(),
                ModItems.TWEED.get().getDefaultInstance(),
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
        );
    }

    private static void insertRareChickenItems(BuildCreativeModeTabContentsEvent event) {
        var visibility = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
        ItemStack rareEgg = ModItems.RARE_EGG.get().getDefaultInstance();
        ItemStack blueEgg = new ItemStack(
                BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace("blue_egg")));
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS
                || event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            if (blueEgg.isEmpty() || !tryInsertAfter(event, blueEgg, rareEgg, visibility)) {
                tryInsertAfter(event, Items.EGG.getDefaultInstance(), rareEgg, visibility);
            }
        }
        ItemStack crate = new ItemStack(
                BuiltInRegistries.ITEM.get(
                        ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "rare_egg_crate")));
        if (crate.isEmpty()) {
            return;
        }
        ItemStack blueCrate = new ItemStack(
                BuiltInRegistries.ITEM.get(
                        ResourceLocation.fromNamespaceAndPath("vbincubationcompat", "blue_egg_crate")));
        ItemStack chickenCrate = new ItemStack(
                BuiltInRegistries.ITEM.get(
                        ResourceLocation.fromNamespaceAndPath("incubation", "chicken_egg_crate")));
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS
                || event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS
                || event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            if (!blueCrate.isEmpty() && tryInsertAfter(event, blueCrate, crate, visibility)) {
                return;
            }
            if (!chickenCrate.isEmpty() && tryInsertAfter(event, chickenCrate, crate, visibility)) {
                return;
            }
            event.accept(crate, visibility);
        }
    }

    private static void insertGoldenMaggot(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.FOOD_AND_DRINKS) {
            return;
        }
        var visibility = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
        ItemStack maggot = ModItems.GOLDEN_MAGGOT.get().getDefaultInstance();
        var fried = BuiltInRegistries.ITEM.getOptional(
                ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "fried_maggot"));
        if (fried.isPresent() && tryInsertAfter(event, fried.get().getDefaultInstance(), maggot, visibility)) {
            return;
        }
        tryInsertAfter(event, Items.COOKED_CHICKEN.getDefaultInstance(), maggot, visibility);
    }
}
