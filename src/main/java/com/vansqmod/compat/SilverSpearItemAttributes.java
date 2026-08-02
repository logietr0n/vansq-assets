package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * Silver spear: extra magic damage on the tooltip; stab damage uses magic via
 * {@link com.vansqmod.mixin.KineticWeaponMixin}. Attack damage stays default (~1 from fist + gold-tier spear).
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class SilverSpearItemAttributes {

    private static final ResourceLocation SILVER_SPEAR = ResourceLocation.withDefaultNamespace("silver_spear");
    private static final ResourceLocation CAVERNS_MAGIC_DAMAGE =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "magic_damage");

    /**
     * Matches iron spear offensive feel (iron tier +2 on top of the +1 fist base); kinetic scaling adds on top.
     */
    private static final double SILVER_SPEAR_MAGIC_DAMAGE = 2.0D;

    private SilverSpearItemAttributes() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        if (!ModList.get().isLoaded("caverns_and_chasms")) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(SILVER_SPEAR)) {
            return;
        }
        var magic = BuiltInRegistries.ATTRIBUTE.getHolder(CAVERNS_MAGIC_DAMAGE).orElse(null);
        if (magic == null) {
            return;
        }

        event.addModifier(
                magic,
                new AttributeModifier(
                        ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "silver_spear_magic_damage"),
                        SILVER_SPEAR_MAGIC_DAMAGE,
                        Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.MAINHAND
        );
    }
}
