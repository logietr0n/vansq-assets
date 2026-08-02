package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * Abnormals Delight registers the necromium knife with bare {@link Item.Properties} (no FD {@code knifeItem} helper),
 * so {@link com.vansqmod.mixin.FarmersDelightKnifeStatsMixin} never runs for it. Match that mixin diamond branch here.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class AbnormalsDelightNecromiumKnifeCompat {

    private static final ResourceLocation NECROMIUM_KNIFE =
            ResourceLocation.fromNamespaceAndPath("abnormals_delight", "necromium_knife");

    /** Matches {@code FarmersDelightKnifeStatsMixin} diamond line ({@code desiredDamage = 1.25F}). */
    private static final float DESIRED_ATTACK_DAMAGE = 1.5F;

    /**
     * {@link net.minecraft.world.item.DiggerItem#createAttributes} stores {@code attackDamage + tier.getAttackDamageBonus()}
     * as one modifier; mixin passes {@code desiredDamage - 1 - tierBonus}, which collapses to {@code desiredDamage - 1}
     * after merging tier bonus. Apply that merged value here.
     */
    private static final double ATTACK_DAMAGE_MODIFIER = DESIRED_ATTACK_DAMAGE - 1.0D;

    /** Same as {@code FarmersDelightKnifeStatsMixin} and silver knife compat: {@code 10 - 4} ADD_VALUE on attack speed. */
    private static final double ATTACK_SPEED_MODIFIER = 6.0D;

    private AbnormalsDelightNecromiumKnifeCompat() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        if (!ModList.get().isLoaded("abnormals_delight")) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !NECROMIUM_KNIFE.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
            return;
        }

        event.replaceModifier(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, ATTACK_DAMAGE_MODIFIER, Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
        event.replaceModifier(
                Attributes.ATTACK_SPEED,
                new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, ATTACK_SPEED_MODIFIER, Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
    }
}
