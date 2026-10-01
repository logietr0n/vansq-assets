package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import com.vansqmod.integration.caverns.CavernsArmorAttributeOverrides;
import com.vansqmod.registry.ModItemTags;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * Pack item stats previously applied through AttributeSetter datapacks.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class PackItemAttributes {

    private static final ResourceLocation BASE_RANGE_ID =
            ResourceLocation.withDefaultNamespace("base_entity_interaction_range");
    private static final ResourceLocation CC_MAGIC_DAMAGE =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "magic_damage");
    private static final ResourceLocation CC_LIFESTEAL =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "lifesteal");
    private static final ResourceLocation CC_SLOWNESS_RETRIBUTION =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "slowness_retribution");

    private PackItemAttributes() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String key = id.toString();

        if (stack.is(ModItemTags.SCYTHE)) {
            replaceAddValue(event, Attributes.ENTITY_INTERACTION_RANGE, 1.0D, EquipmentSlotGroup.MAINHAND, BASE_RANGE_ID);
            replaceAddValue(event, Attributes.ATTACK_SPEED, -3.2D, EquipmentSlotGroup.MAINHAND, Item.BASE_ATTACK_SPEED_ID);
        }

        switch (key) {
            case "born_in_chaos_v1:nightmare_scythe" ->
                    replaceAddValue(event, Attributes.ATTACK_DAMAGE, 6.0D, EquipmentSlotGroup.MAINHAND, Item.BASE_ATTACK_DAMAGE_ID);
            case "minecraft:rose_gold_spear" ->
                    addValue(event, Attributes.ATTACK_DAMAGE, -0.5D, EquipmentSlotGroup.MAINHAND, "rose_gold_spear_damage");
            case "minecraft:silver_spear" ->
                    addCc(event, CC_MAGIC_DAMAGE, 2.0D, EquipmentSlotGroup.MAINHAND, "silver_spear_magic");
            case "abnormals_delight:silver_knife" -> {
                replaceAddValue(event, Attributes.ATTACK_DAMAGE, -1.0D, EquipmentSlotGroup.MAINHAND, Item.BASE_ATTACK_DAMAGE_ID);
                replaceAddValue(event, Attributes.ATTACK_SPEED, 6.0D, EquipmentSlotGroup.MAINHAND, Item.BASE_ATTACK_SPEED_ID);
                addCc(event, CC_MAGIC_DAMAGE, 1.0D, EquipmentSlotGroup.MAINHAND, "silver_knife_magic");
            }
            case "abnormals_delight:necromium_knife" -> {
                replaceAddValue(event, Attributes.ATTACK_DAMAGE, 0.5D, EquipmentSlotGroup.MAINHAND, Item.BASE_ATTACK_DAMAGE_ID);
                replaceAddValue(event, Attributes.ATTACK_SPEED, 6.0D, EquipmentSlotGroup.MAINHAND, Item.BASE_ATTACK_SPEED_ID);
            }
            case "minecraft:copper_sword" ->
                    replaceAddValue(event, Attributes.ATTACK_DAMAGE, 4.5D, EquipmentSlotGroup.MAINHAND, Item.BASE_ATTACK_DAMAGE_ID);
            case "minecraft:copper_pickaxe" ->
                    replaceAddValue(event, Attributes.ATTACK_DAMAGE, 2.5D, EquipmentSlotGroup.MAINHAND, Item.BASE_ATTACK_DAMAGE_ID);
            case "minecraft:copper_shovel" ->
                    replaceAddValue(event, Attributes.ATTACK_DAMAGE, 3.0D, EquipmentSlotGroup.MAINHAND, Item.BASE_ATTACK_DAMAGE_ID);
            case "minecraft:copper_axe" ->
                    replaceAddValue(event, Attributes.ATTACK_SPEED, -3.15D, EquipmentSlotGroup.MAINHAND, Item.BASE_ATTACK_SPEED_ID);
            case "minecraft:copper_hoe" ->
                    replaceAddValue(event, Attributes.ATTACK_SPEED, -1.5D, EquipmentSlotGroup.MAINHAND, Item.BASE_ATTACK_SPEED_ID);
            default -> {
            }
        }

        if (isCcCopperArmor(id)) {
            addValue(event, Attributes.ARMOR_TOUGHNESS, 1.0D, armorSlot(stack), "copper_armor_toughness." + piece(stack));
        }

        applySterlingArmor(event, key, stack);
        applySanguineArmor(event, stack);
        applyChaosArmor(event, key, stack);
        applyEchoArmor(event, key, stack);
        applyNecromiumArmor(event, key, stack);
        applyNetheriteArmor(event, key, stack);
    }

    private static void applySterlingArmor(ItemAttributeModifierEvent event, String key, ItemStack stack) {
        double armor = switch (key) {
            case "galosphere:sterling_helmet", "galosphere:sterling_boots" -> 2.0D;
            case "galosphere:sterling_chestplate" -> 6.0D;
            case "galosphere:sterling_leggings" -> 5.0D;
            default -> Double.NaN;
        };
        if (Double.isNaN(armor)) {
            return;
        }
        EquipmentSlotGroup slot = armorSlot(stack);
        replaceAddValue(event, Attributes.ARMOR, armor, slot, null);
        addValue(event, Attributes.ARMOR_TOUGHNESS, 2.0D, slot, "sterling_toughness." + piece(stack));
    }

    private static void applySanguineArmor(ItemAttributeModifierEvent event, ItemStack stack) {
        if (!CavernsArmorAttributeOverrides.isSanguinePiece(stack)) {
            return;
        }
        String piece = piece(stack);
        double armor = switch (piece) {
            case "helmet" -> 2.0D;
            case "chestplate" -> 5.0D;
            case "leggings" -> 4.0D;
            case "boots" -> 1.0D;
            default -> Double.NaN;
        };
        double health = switch (piece) {
            case "helmet", "boots" -> 2.0D;
            case "chestplate", "leggings" -> 3.0D;
            default -> Double.NaN;
        };
        if (Double.isNaN(armor) || Double.isNaN(health)) {
            return;
        }
        EquipmentSlotGroup slot = armorSlot(stack);
        replaceAddValue(event, Attributes.ARMOR, armor, slot, null);
        addValue(event, Attributes.MAX_HEALTH, health, slot, "sanguine_health." + piece);
        addCc(event, CC_LIFESTEAL, 0.05D, slot, "sanguine_lifesteal." + piece, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    private static void applyChaosArmor(ItemAttributeModifierEvent event, String key, ItemStack stack) {
        double armorAdd = switch (key) {
            case "born_in_chaos_v1:dark_metal_armor_helmet", "born_in_chaos_v1:dark_metal_armor_leggings" -> -1.0D;
            case "born_in_chaos_v1:dark_metal_armor_chestplate" -> -2.0D;
            case "born_in_chaos_v1:dark_metal_armor_boots" -> 0.0D;
            default -> Double.NaN;
        };
        if (Double.isNaN(armorAdd)) {
            return;
        }
        EquipmentSlotGroup slot = armorSlot(stack);
        if (armorAdd != 0.0D) {
            addValue(event, Attributes.ARMOR, armorAdd, slot, "chaos_armor." + piece(stack));
        }
        event.removeAllModifiersFor(Attributes.KNOCKBACK_RESISTANCE);
        addCc(event, CC_SLOWNESS_RETRIBUTION, 1.0D, slot, "chaos_slowness_retribution." + piece(stack));
    }

    private static void applyEchoArmor(ItemAttributeModifierEvent event, String key, ItemStack stack) {
        double armorAdd = switch (key) {
            case "deeperdarker:warden_helmet", "deeperdarker:warden_chestplate" -> 2.0D;
            case "deeperdarker:warden_leggings", "deeperdarker:warden_boots" -> 1.0D;
            default -> Double.NaN;
        };
        if (Double.isNaN(armorAdd)) {
            return;
        }
        EquipmentSlotGroup slot = armorSlot(stack);
        String piece = piece(stack);
        addValue(event, Attributes.ARMOR, armorAdd, slot, "echo_armor." + piece);
        addValue(event, Attributes.ARMOR_TOUGHNESS, 1.0D, slot, "echo_toughness." + piece);
        if ("deeperdarker:warden_leggings".equals(key)) {
            event.removeAllModifiersFor(Attributes.MOVEMENT_SPEED);
        }
    }

    private static void applyNecromiumArmor(ItemAttributeModifierEvent event, String key, ItemStack stack) {
        double toughness = switch (key) {
            case "caverns_and_chasms:necromium_helmet", "caverns_and_chasms:necromium_boots" -> 1.0D;
            case "caverns_and_chasms:necromium_chestplate", "caverns_and_chasms:necromium_leggings" -> 2.0D;
            default -> Double.NaN;
        };
        if (Double.isNaN(toughness)) {
            return;
        }
        addValue(event, Attributes.ARMOR_TOUGHNESS, toughness, armorSlot(stack), "necromium_toughness." + piece(stack));
    }

    private static void applyNetheriteArmor(ItemAttributeModifierEvent event, String key, ItemStack stack) {
        double armorAdd = switch (key) {
            case "minecraft:netherite_helmet", "minecraft:netherite_leggings", "minecraft:netherite_boots" -> 1.0D;
            case "minecraft:netherite_chestplate" -> 2.0D;
            default -> Double.NaN;
        };
        if (Double.isNaN(armorAdd)) {
            return;
        }
        addValue(event, Attributes.ARMOR, armorAdd, armorSlot(stack), "netherite_armor." + piece(stack));
    }

    private static boolean isCcCopperArmor(ResourceLocation id) {
        if (!"caverns_and_chasms".equals(id.getNamespace())) {
            return false;
        }
        String path = id.getPath();
        return path.equals("copper_helmet")
                || path.equals("copper_chestplate")
                || path.equals("copper_leggings")
                || path.equals("copper_boots");
    }

    private static void replaceAddValue(
            ItemAttributeModifierEvent event,
            Holder<Attribute> attribute,
            double amount,
            EquipmentSlotGroup slot,
            ResourceLocation preferredId
    ) {
        ResourceLocation id = preferredId;
        if (id == null) {
            for (ItemAttributeModifiers.Entry entry : event.getModifiers()) {
                if (entry.attribute().is(attribute)
                        && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                    id = entry.modifier().id();
                    break;
                }
            }
        }
        if (id == null) {
            id = ResourceLocation.fromNamespaceAndPath(
                    VansqMod.MODID,
                    "base_" + attribute.unwrapKey().map(key -> key.location().getPath()).orElse("attribute")
            );
        }
        event.replaceModifier(
                attribute,
                new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE),
                slot
        );
    }

    private static void addValue(
            ItemAttributeModifierEvent event,
            Holder<Attribute> attribute,
            double amount,
            EquipmentSlotGroup slot,
            String key
    ) {
        event.replaceModifier(
                attribute,
                new AttributeModifier(
                        ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, key),
                        amount,
                        AttributeModifier.Operation.ADD_VALUE
                ),
                slot
        );
    }

    private static void addCc(
            ItemAttributeModifierEvent event,
            ResourceLocation attributeId,
            double amount,
            EquipmentSlotGroup slot,
            String key
    ) {
        addCc(event, attributeId, amount, slot, key, AttributeModifier.Operation.ADD_VALUE);
    }

    private static void addCc(
            ItemAttributeModifierEvent event,
            ResourceLocation attributeId,
            double amount,
            EquipmentSlotGroup slot,
            String key,
            AttributeModifier.Operation operation
    ) {
        BuiltInRegistries.ATTRIBUTE.getHolder(attributeId).ifPresent(attribute ->
                event.replaceModifier(
                        attribute,
                        new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, key),
                                amount,
                                operation
                        ),
                        slot
                )
        );
    }

    private static EquipmentSlotGroup armorSlot(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armor) {
            return EquipmentSlotGroup.bySlot(armor.getEquipmentSlot());
        }
        return EquipmentSlotGroup.ARMOR;
    }

    private static String piece(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armor) {
            return armor.getType().getName();
        }
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (path.contains("helmet")) {
            return "helmet";
        }
        if (path.contains("chestplate")) {
            return "chestplate";
        }
        if (path.contains("leggings")) {
            return "leggings";
        }
        if (path.contains("boots")) {
            return "boots";
        }
        return "armor";
    }
}
