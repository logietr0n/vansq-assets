package com.vansqmod.entity;

import com.vansqmod.debug.VansqDebugState;
import com.vansqmod.registry.ModBiomeTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;

/**
 * Zombie-family mainhand spawn rolls. Iron sword / spear / shovel / knife keep the
 * vanilla+Barched 1%/5% gate; copper copies are extra at 1.5× each iron counterpart.
 * Gold tools only roll in {@link ModBiomeTags#ZOMBIE_GOLD_TOOLS}; silver tools only
 * in {@link ModBiomeTags#ZOMBIE_SILVER_TOOLS}.
 */
public final class MobSpawnWeapons {

    private static final int IRON_SWORD_WEIGHT = 2;
    private static final int COPPER_SWORD_WEIGHT = 3;
    private static final int IRON_SPEAR_WEIGHT = 2;
    private static final int COPPER_SPEAR_WEIGHT = 3;
    private static final int IRON_SHOVEL_WEIGHT = 8;
    private static final int COPPER_SHOVEL_WEIGHT = 12;
    private static final int IRON_KNIFE_WEIGHT = 8;
    private static final int COPPER_KNIFE_WEIGHT = 12;
    private static final int GOLD_SHOVEL_WEIGHT = 4;
    private static final int GOLD_KNIFE_WEIGHT = 4;
    private static final int GOLD_SWORD_WEIGHT = 1;
    private static final int GOLD_SPEAR_WEIGHT = 1;
    private static final int SILVER_SHOVEL_WEIGHT = 4;
    private static final int SILVER_KNIFE_WEIGHT = 4;
    private static final int SILVER_SWORD_WEIGHT = 1;
    private static final int SILVER_SPEAR_WEIGHT = 1;

    private MobSpawnWeapons() {
    }

    public static ItemStack rollZombieMainhand(LivingEntity mob, RandomSource random, DifficultyInstance difficulty) {
        float ironGate = difficulty.getDifficulty() == Difficulty.HARD ? 0.05F : 0.01F;
        boolean goldBiome = inTag(mob, ModBiomeTags.ZOMBIE_GOLD_TOOLS);
        boolean silverBiome = inTag(mob, ModBiomeTags.ZOMBIE_SILVER_TOOLS);

        List<Choice> choices = new ArrayList<>();
        add(choices, Items.IRON_SWORD, IRON_SWORD_WEIGHT, true);
        add(choices, vanillaItem("copper_sword"), COPPER_SWORD_WEIGHT, false);
        add(choices, vanillaItem("iron_spear"), IRON_SPEAR_WEIGHT, true);
        add(choices, vanillaItem("copper_spear"), COPPER_SPEAR_WEIGHT, false);
        add(choices, Items.IRON_SHOVEL, IRON_SHOVEL_WEIGHT, true);
        add(choices, vanillaItem("copper_shovel"), COPPER_SHOVEL_WEIGHT, false);
        add(choices, item("farmersdelight", "iron_knife"), IRON_KNIFE_WEIGHT, true);
        add(choices, item("vansqmod", "copper_knife"), COPPER_KNIFE_WEIGHT, false);

        if (goldBiome) {
            add(choices, Items.GOLDEN_SHOVEL, GOLD_SHOVEL_WEIGHT, false);
            add(choices, item("farmersdelight", "golden_knife"), GOLD_KNIFE_WEIGHT, false);
            add(choices, Items.GOLDEN_SWORD, GOLD_SWORD_WEIGHT, false);
            add(choices, vanillaItem("golden_spear"), GOLD_SPEAR_WEIGHT, false);
        }
        if (silverBiome) {
            add(choices, item("caverns_and_chasms", "silver_shovel"), SILVER_SHOVEL_WEIGHT, false);
            add(choices, item("abnormals_delight", "silver_knife"), SILVER_KNIFE_WEIGHT, false);
            add(choices, item("caverns_and_chasms", "silver_sword"), SILVER_SWORD_WEIGHT, false);
            Item silverSpear = item("caverns_and_chasms", "silver_spear");
            if (!present(silverSpear)) {
                silverSpear = vanillaItem("silver_spear");
            }
            add(choices, silverSpear, SILVER_SPEAR_WEIGHT, false);
        }

        int total = 0;
        int ironOnly = 0;
        for (Choice choice : choices) {
            total += choice.weight;
            if (choice.ironBase) {
                ironOnly += choice.weight;
            }
        }
        if (total <= 0 || ironOnly <= 0) {
            return ItemStack.EMPTY;
        }

        float armedChance = ironGate * ((float) total / (float) ironOnly);
        if (!VansqDebugState.isForceEquipmentEnabled() && random.nextFloat() >= armedChance) {
            return ItemStack.EMPTY;
        }

        int roll = random.nextInt(total);
        for (Choice choice : choices) {
            roll -= choice.weight;
            if (roll < 0) {
                return new ItemStack(choice.item);
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean inTag(LivingEntity mob, TagKey<Biome> tag) {
        return mob.level().getBiome(mob.blockPosition()).is(tag);
    }

    private static void add(List<Choice> choices, Item item, int weight, boolean ironBase) {
        if (weight > 0 && present(item)) {
            choices.add(new Choice(item, weight, ironBase));
        }
    }

    private static Item vanillaItem(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(path));
    }

    private static Item item(String namespace, String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static boolean present(Item item) {
        return item != null && item != Items.AIR;
    }

    private record Choice(Item item, int weight, boolean ironBase) {
    }
}
