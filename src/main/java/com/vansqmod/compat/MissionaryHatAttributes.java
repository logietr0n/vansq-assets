package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import com.vansqmod.integration.missionaryhat.MissionaryHatEquipment;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * Missionary hat is a disguise, not armor: strip defense in the helmet slot and Curios {@code head}.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class MissionaryHatAttributes {

    private MissionaryHatAttributes() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        if (!MissionaryHatEquipment.isHat(event.getItemStack())) {
            return;
        }
        event.removeAllModifiersFor(Attributes.ARMOR);
        event.removeAllModifiersFor(Attributes.ARMOR_TOUGHNESS);
        event.removeAllModifiersFor(Attributes.KNOCKBACK_RESISTANCE);
    }
}
