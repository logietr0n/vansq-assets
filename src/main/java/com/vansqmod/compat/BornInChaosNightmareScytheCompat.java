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
 * Born in Chaos nightmare scythe (wither scythe): pack balance uses 7 attack damage.
 * Reach (+1.0 entity interaction range) is applied by {@link ScytheReachCompat}.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class BornInChaosNightmareScytheCompat {

    private static final ResourceLocation NIGHTMARE_SCYTHE =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "nightmare_scythe");

    /** Final attack damage = 1.0 (player base) + this modifier. */
    private static final double ATTACK_DAMAGE_MODIFIER = 7.0D - 1.0D;

    private BornInChaosNightmareScytheCompat() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        if (!ModList.get().isLoaded("born_in_chaos_v1")) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !NIGHTMARE_SCYTHE.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
            return;
        }

        event.replaceModifier(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, ATTACK_DAMAGE_MODIFIER, Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
        );
    }
}
