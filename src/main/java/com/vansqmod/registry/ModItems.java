package com.vansqmod.registry;

import com.vansqmod.VansqMod;
import com.vansqmod.item.RhodoheartShardItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(VansqMod.MODID);

    // Raw Rose Gold
    public static final DeferredItem<Item> RAW_ROSE_GOLD =
            ITEMS.register("raw_rose_gold",
                    () -> new Item(new Item.Properties()));

    // Rose Gold Ingot
    public static final DeferredItem<Item> ROSE_GOLD_INGOT =
            ITEMS.register("rose_gold_ingot",
                    () -> new Item(new Item.Properties()));

    // Rose Gold Nugget
    public static final DeferredItem<Item> ROSE_GOLD_NUGGET =
            ITEMS.register("rose_gold_nugget",
                    () -> new Item(new Item.Properties()));

    // Rhodoheart Shard
    public static final DeferredItem<RhodoheartShardItem> RHODOHEART_SHARD =
            ITEMS.register("rhodoheart_shard",
                    () -> new RhodoheartShardItem(new Item.Properties()));

    // Rose Gold Tools
    public static final DeferredItem<SwordItem> ROSE_GOLD_SWORD =
            ITEMS.register("rose_gold_sword",
                    () -> new SwordItem(ModTiers.ROSE_GOLD,
                            new Item.Properties()
                                    .attributes(SwordItem.createAttributes(ModTiers.ROSE_GOLD, 3, -2.4F))));

    // Rose Gold Knife (Farmer's Delight-compatible via item tag)
    public static final DeferredItem<SwordItem> ROSE_GOLD_KNIFE =
            ITEMS.register("rose_gold_knife",
                    () -> new SwordItem(ModTiers.ROSE_GOLD,
                            new Item.Properties()
                                    // Final attack damage = 1.0 + tierBonus + itemBonus = 1.25
                                    .attributes(SwordItem.createAttributes(ModTiers.ROSE_GOLD, -2.25F, 6.0F))));

    // Copper Knife (Farmer's Delight-compatible via item tag)
    public static final DeferredItem<SwordItem> COPPER_KNIFE =
            ITEMS.register("copper_knife",
                    () -> new SwordItem(ModTiers.COPPER,
                            new Item.Properties()
                                    // Final attack damage = 1.0 + 0.5 + (-0.75) = 0.75; speed = 4 + 6 = 10
                                    .attributes(SwordItem.createAttributes(ModTiers.COPPER, -0.75F, 6.0F))));

    /** Matches Combat Nouveau / vanilla base entity-interaction-range modifier id. */
    private static final ResourceLocation BASE_ENTITY_INTERACTION_RANGE_ID =
            ResourceLocation.withDefaultNamespace("base_entity_interaction_range");

    // Scythe — sweeping weapon; final damage 5.0, attack speed 0.8 (4.0 + -3.2);
    // +1.0 entity interaction range → 4.0 total reach (and matching sweep radius)
    public static final DeferredItem<SwordItem> SCYTHE =
            ITEMS.register("scythe",
                    () -> new SwordItem(ModTiers.SCYTHE,
                            new Item.Properties()
                                    .attributes(scytheAttributes())));

    private static ItemAttributeModifiers scytheAttributes() {
        return SwordItem.createAttributes(ModTiers.SCYTHE, 4.0F, -3.2F)
                .withModifierAdded(
                        Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(BASE_ENTITY_INTERACTION_RANGE_ID, 1.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                );
    }

    public static final DeferredItem<PickaxeItem> ROSE_GOLD_PICKAXE =
            ITEMS.register("rose_gold_pickaxe",
                    () -> new PickaxeItem(ModTiers.ROSE_GOLD,
                            new Item.Properties()
                                    .attributes(PickaxeItem.createAttributes(ModTiers.ROSE_GOLD, 1.0F, -2.8F))));

    public static final DeferredItem<AxeItem> ROSE_GOLD_AXE =
            ITEMS.register("rose_gold_axe",
                    () -> new AxeItem(ModTiers.ROSE_GOLD,
                            new Item.Properties()
                                    .attributes(AxeItem.createAttributes(ModTiers.ROSE_GOLD, 5.5F, -3.05F))));

    public static final DeferredItem<ShovelItem> ROSE_GOLD_SHOVEL =
            ITEMS.register("rose_gold_shovel",
                    () -> new ShovelItem(ModTiers.ROSE_GOLD,
                            new Item.Properties()
                                    .attributes(ShovelItem.createAttributes(ModTiers.ROSE_GOLD, 1.5F, -3.0F))));

    public static final DeferredItem<HoeItem> ROSE_GOLD_HOE =
            ITEMS.register("rose_gold_hoe",
                    () -> new HoeItem(ModTiers.ROSE_GOLD,
                            new Item.Properties()
                                    // Final attack damage = 1.0 + tierBonus + itemBonus = 1.0
                                    .attributes(HoeItem.createAttributes(ModTiers.ROSE_GOLD, -2.5F, -0.5F))));

    public static Item ROSE_GOLD_SPEAR;
    public static Item SILVER_SPEAR;
    public static Item NECROMIUM_SPEAR;

    public static final DeferredItem<SmithingTemplateItem> DEEPSLATE_UPGRADE_SMITHING_TEMPLATE =
            ITEMS.register("deepslate_upgrade_smithing_template", () -> new SmithingTemplateItem(
                    Component.translatable("item.vansqmod.deepslate_upgrade.applies_to").withStyle(ChatFormatting.BLUE),
                    Component.translatable("item.vansqmod.deepslate_upgrade.ingredients").withStyle(ChatFormatting.BLUE),
                    Component.translatable("upgrade.vansqmod.deepslate_upgrade").withStyle(ChatFormatting.GRAY),
                    Component.translatable("item.vansqmod.deepslate_upgrade.base_slot_description"),
                    Component.translatable("item.vansqmod.deepslate_upgrade.additions_slot_description"),
                    List.of(ResourceLocation.withDefaultNamespace("item/empty_slot_pickaxe")),
                    List.of(ResourceLocation.withDefaultNamespace("item/empty_slot_ingot"))
            ));

    // Rose Gold Armor
    public static final DeferredItem<ArmorItem> ROSE_GOLD_HELMET =
            ITEMS.register("rose_gold_helmet",
                    () -> new ArmorItem(ModArmorMaterials.ROSE_GOLD_ARMOR_MATERIAL,
                            ArmorItem.Type.HELMET,
                            new Item.Properties().durability(253)));

    public static final DeferredItem<ArmorItem> ROSE_GOLD_CHESTPLATE =
            ITEMS.register("rose_gold_chestplate",
                    () -> new ArmorItem(ModArmorMaterials.ROSE_GOLD_ARMOR_MATERIAL,
                            ArmorItem.Type.CHESTPLATE,
                            new Item.Properties().durability(368)));

    public static final DeferredItem<ArmorItem> ROSE_GOLD_LEGGINGS =
            ITEMS.register("rose_gold_leggings",
                    () -> new ArmorItem(ModArmorMaterials.ROSE_GOLD_ARMOR_MATERIAL,
                            ArmorItem.Type.LEGGINGS,
                            new Item.Properties().durability(345)));

    public static final DeferredItem<ArmorItem> ROSE_GOLD_BOOTS =
            ITEMS.register("rose_gold_boots",
                    () -> new ArmorItem(ModArmorMaterials.ROSE_GOLD_ARMOR_MATERIAL,
                            ArmorItem.Type.BOOTS,
                            new Item.Properties().durability(299)));
}