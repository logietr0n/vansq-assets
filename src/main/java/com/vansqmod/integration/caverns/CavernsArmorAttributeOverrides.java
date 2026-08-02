package com.vansqmod.integration.caverns;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * Normalizes Caverns &amp; Chasms armor attack-damage bonuses to +5% per piece (+20% full set).
 */
public final class CavernsArmorAttributeOverrides {

    public static final double ATTACK_DAMAGE_PERCENT_PER_PIECE = 0.05D;

    private CavernsArmorAttributeOverrides() {
    }

    /**
     * Strips every attack-damage modifier on the item, then applies a single +5% bonus.
     * Prevents duplicate attack-damage modifiers from stacking.
     */
    public static void enforceAttackDamagePerPiece(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!isChainmailPiece(stack) && !isSanguinePiece(stack)) {
            return;
        }

        if (!(stack.getItem() instanceof ArmorItem armor)) {
            return;
        }

        EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(armor.getEquipmentSlot());
        String armorKind = isChainmailPiece(stack) ? "chainmail" : "sanguine";
        ResourceLocation modifierId = vansqAttackDamageId(armor.getType(), armorKind);

        event.removeIf(entry -> entry.attribute().is(Attributes.ATTACK_DAMAGE));
        event.addModifier(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(
                        modifierId,
                        ATTACK_DAMAGE_PERCENT_PER_PIECE,
                        Operation.ADD_MULTIPLIED_BASE
                ),
                group
        );
    }

    public static boolean isSanguinePiece(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return "caverns_and_chasms".equals(id.getNamespace()) && id.getPath().startsWith("sanguine_");
    }

    public static boolean isChainmailPiece(ItemStack stack) {
        return stack.is(Items.CHAINMAIL_HELMET)
                || stack.is(Items.CHAINMAIL_CHESTPLATE)
                || stack.is(Items.CHAINMAIL_LEGGINGS)
                || stack.is(Items.CHAINMAIL_BOOTS);
    }

    public static ResourceLocation vansqAttackDamageId(ArmorItem.Type armorType, String armorKind) {
        return ResourceLocation.fromNamespaceAndPath(
                VansqMod.MODID,
                armorKind + "_attack_damage." + armorType.getName()
        );
    }
}
