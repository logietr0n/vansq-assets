package com.vansqmod.entity;

import com.vansqmod.registry.ModBiomeTags;
import com.vansqmod.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;

/**
 * Weighted spawn-armor tiers. Gold is warm/desert/badlands only; silver is
 * cold/snowy/icy only. One roll is reused for every piece on the same mob.
 */
public final class MobSpawnArmor {

    /** Vanilla {@link net.minecraft.world.entity.Mob#getEquipmentForSlot} gold. */
    public static final int GOLD_QUALITY = 1;
    /** Vanilla chain. */
    public static final int CHAIN_QUALITY = 2;
    /** Vanilla iron. */
    public static final int IRON_QUALITY = 3;
    /** Vanilla diamond. */
    public static final int DIAMOND_QUALITY = 4;
    /** Copper Age Backport copper. */
    public static final int COPPER_QUALITY = 5;
    /** vansqmod rose gold. */
    public static final int ROSE_GOLD_QUALITY = 6;
    /** Caverns &amp; Chasms silver. */
    public static final int SILVER_QUALITY = 7;
    /** Vanilla leather. */
    public static final int LEATHER_QUALITY = 0;

    private static final int LEATHER_WEIGHT = 11;
    private static final int COPPER_WEIGHT = 12;
    private static final int CHAIN_WEIGHT = 9;
    private static final int GOLD_WEIGHT = 10;
    private static final int SILVER_WEIGHT = 10;
    private static final int IRON_WEIGHT = 8;
    private static final int ROSE_GOLD_WEIGHT = 4;
    private static final int DIAMOND_WEIGHT = 1;

    private static final ThreadLocal<Integer> SPAWN_QUALITY = new ThreadLocal<>();

    private MobSpawnArmor() {
    }

    public static void endSpawnRoll() {
        SPAWN_QUALITY.remove();
    }

    /**
     * C&amp;C swaps chainmail to its oxidizing copper set after vanilla equipment
     * is assigned. Undo that so only {@code minecraft:copper_*} from
     * {@link #copperItem} is used for the copper spawn tier.
     */
    public static void restoreChainmailFromCcCopper(Mob mob) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) {
                continue;
            }
            ItemStack stack = mob.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!"caverns_and_chasms".equals(id.getNamespace()) || !isCcCopperArmor(id.getPath())) {
                continue;
            }
            Item chain = chainmailItem(slot);
            if (chain != null) {
                mob.setItemSlot(slot, new ItemStack(chain));
            }
        }
    }

    public static int currentQuality(Mob mob, RandomSource random, int vanillaQuality) {
        Integer rolled = SPAWN_QUALITY.get();
        if (rolled == null) {
            rolled = rollQuality(mob, random);
            SPAWN_QUALITY.set(rolled);
        }
        return rolled;
    }

    public static Item extraTierItem(EquipmentSlot slot, int quality) {
        if (quality == COPPER_QUALITY) {
            Item item = copperItem(slot);
            return item == Items.AIR ? null : item;
        }
        if (quality == ROSE_GOLD_QUALITY) {
            return roseGoldItem(slot);
        }
        if (quality == SILVER_QUALITY) {
            Item item = silverItem(slot);
            return item == Items.AIR ? null : item;
        }
        return null;
    }

    private static int rollQuality(Mob mob, RandomSource random) {
        boolean goldBiome = inTag(mob, ModBiomeTags.ZOMBIE_GOLD_TOOLS);
        boolean silverBiome = inTag(mob, ModBiomeTags.ZOMBIE_SILVER_TOOLS);

        List<Choice> choices = new ArrayList<>();
        add(choices, LEATHER_QUALITY, LEATHER_WEIGHT, true);
        add(choices, COPPER_QUALITY, COPPER_WEIGHT, copperItem(EquipmentSlot.HEAD) != Items.AIR);
        add(choices, CHAIN_QUALITY, CHAIN_WEIGHT, true);
        add(choices, GOLD_QUALITY, GOLD_WEIGHT, goldBiome);
        add(choices, SILVER_QUALITY, SILVER_WEIGHT, silverBiome && silverItem(EquipmentSlot.HEAD) != Items.AIR);
        add(choices, IRON_QUALITY, IRON_WEIGHT, true);
        add(choices, ROSE_GOLD_QUALITY, ROSE_GOLD_WEIGHT, roseGoldItem(EquipmentSlot.HEAD) != null);
        add(choices, DIAMOND_QUALITY, DIAMOND_WEIGHT, true);

        int total = 0;
        for (Choice choice : choices) {
            total += choice.weight;
        }
        if (total <= 0) {
            return LEATHER_QUALITY;
        }

        int roll = random.nextInt(total);
        for (Choice choice : choices) {
            roll -= choice.weight;
            if (roll < 0) {
                return choice.quality;
            }
        }
        return LEATHER_QUALITY;
    }

    private static void add(List<Choice> choices, int quality, int weight, boolean enabled) {
        if (enabled && weight > 0) {
            choices.add(new Choice(quality, weight));
        }
    }

    private static boolean inTag(Mob mob, TagKey<Biome> tag) {
        return mob.level().getBiome(mob.blockPosition()).is(tag);
    }

    private static Item copperItem(EquipmentSlot slot) {
        String path = switch (slot) {
            case HEAD -> "copper_helmet";
            case CHEST -> "copper_chestplate";
            case LEGS -> "copper_leggings";
            case FEET -> "copper_boots";
            default -> null;
        };
        if (path == null) {
            return Items.AIR;
        }
        return BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(path));
    }

    private static Item silverItem(EquipmentSlot slot) {
        String path = switch (slot) {
            case HEAD -> "silver_helmet";
            case CHEST -> "silver_chestplate";
            case LEGS -> "silver_leggings";
            case FEET -> "silver_boots";
            default -> null;
        };
        if (path == null) {
            return Items.AIR;
        }
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", path));
    }

    private static boolean isCcCopperArmor(String path) {
        return path.equals("copper_helmet")
                || path.equals("copper_chestplate")
                || path.equals("copper_leggings")
                || path.equals("copper_boots");
    }

    private static Item chainmailItem(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> Items.CHAINMAIL_HELMET;
            case CHEST -> Items.CHAINMAIL_CHESTPLATE;
            case LEGS -> Items.CHAINMAIL_LEGGINGS;
            case FEET -> Items.CHAINMAIL_BOOTS;
            default -> null;
        };
    }

    private static Item roseGoldItem(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> ModItems.ROSE_GOLD_HELMET.get();
            case CHEST -> ModItems.ROSE_GOLD_CHESTPLATE.get();
            case LEGS -> ModItems.ROSE_GOLD_LEGGINGS.get();
            case FEET -> ModItems.ROSE_GOLD_BOOTS.get();
            default -> null;
        };
    }

    private record Choice(int quality, int weight) {
    }
}
