package com.vansqmod.registry;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Magic-damage conversion for the silver crown. Fortune and looting are Curios loot
 * levels, not attributes, so they stay out of Quark's attribute tooltips.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class ModAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, VansqMod.MODID);

    /**
     * Fraction of attack damage converted into magic damage. Not Caverns &amp; Chasms
     * {@code magic_damage}, which is a flat follow-up hit. Kept off the item so Quark
     * does not draw it; the line belongs in {@code vansq_tooltips.json}.
     */
    public static final DeferredHolder<Attribute, Attribute> MAGIC_CONVERSION =
            ATTRIBUTES.register("magic_conversion", () -> new RangedAttribute(
                    "attribute.vansqmod.magic_conversion", 0.0D, 0.0D, 1.0D).setSyncable(true));

    private ModAttributes() {
    }

    @SubscribeEvent
    public static void addToPlayers(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, MAGIC_CONVERSION);
    }
}
