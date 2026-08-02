package com.vansqmod.registry;

import net.minecraft.tags.BlockTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class ModTiers {

    /**
     * Vanilla-aligned copper tool tier (1.21.2+ style). Used for add-on knives on 1.21.1 where {@code Tiers.COPPER} does not exist.
     */
    public static final Tier COPPER = new Tier() {
        @Override
        public int getUses() {
            return 190;
        }

        @Override
        public float getSpeed() {
            return 5.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 0.5F;
        }

        @Override
        public int getEnchantmentValue() {
            return 13;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.COPPER_INGOT);
        }

        @Override
        public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_STONE_TOOL;
        }
    };

    /**
     * Standalone scythe tier: zero attack-damage bonus so item attributes set the final damage directly
     * ({@code final = 1.0 + itemBonus}).
     */
    public static final Tier SCYTHE = new Tier() {
        @Override
        public int getUses() {
            return 354;
        }

        @Override
        public float getSpeed() {
            return 6.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 0.0F;
        }

        @Override
        public int getEnchantmentValue() {
            return 14;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.IRON_INGOT);
        }

        @Override
        public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_IRON_TOOL;
        }
    };

    public static final Tier ROSE_GOLD = new Tier() {

        public int getUses() {
            return 383;
        }

        public float getSpeed() {
            return 10.0F; // faster than diamond (8) slower than gold (12)
        }

        public float getAttackDamageBonus() {
            // NOTE: Total tool attack damage is computed as: 1.0 (base) + tierBonus + itemBonus.
            // This tierBonus previously being 3.5F made all rose gold tools deal +1 damage
            // compared to intended (e.g. pickaxe 5.5 instead of 4.5).
            return 2.5F;
        }

        public int getLevel() {
            return 2;
        }

        public int getEnchantmentValue() {
            return 22; // same enchantability as gold
        }

        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.ROSE_GOLD_INGOT.get());
        }

        public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
        }

    };

    /**
     * Caverns & Chasms-aligned silver tier, defined locally so we don't need a compile-time dependency on that mod.
     * (Uses "c:ingots/silver" as the repair ingredient, which is a common convention in modpacks.)
     */
    public static final Tier SILVER = new Tier() {
        @Override
        public int getUses() {
            return 157;
        }

        @Override
        public float getSpeed() {
            return 9.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 1.0F;
        }

        @Override
        public int getEnchantmentValue() {
            return 18;
        }

        @Override
        public Ingredient getRepairIngredient() {
            // Prefer the common item tag if present.
            try {
                return Ingredient.of(net.minecraft.tags.ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/silver")));
            } catch (Exception e) {
                return Ingredient.EMPTY;
            }
        }

        @Override
        public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
            // Silver behaves like an iron-ish mining tier.
            return BlockTags.INCORRECT_FOR_IRON_TOOL;
        }
    };

    /**
     * Caverns &amp; Chasms-aligned necromium tier ({@code BlueprintItemTier}), defined locally so we do not need a compile-time dependency on CC.
     */
    public static final Tier NECROMIUM = new Tier() {
        @Override
        public int getUses() {
            return 2031;
        }

        @Override
        public float getSpeed() {
            return 9.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 3.0F;
        }

        @Override
        public int getEnchantmentValue() {
            return 15;
        }

        @Override
        public Ingredient getRepairIngredient() {
            try {
                return Ingredient.of(net.minecraft.tags.ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/necromium")));
            } catch (Exception e) {
                return Ingredient.EMPTY;
            }
        }

        @Override
        public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        }
    };

}