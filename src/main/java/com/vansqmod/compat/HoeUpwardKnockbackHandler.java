package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

/**
 * Applies Combat Nouveau-style upward knockback for {@code #minecraft:hoes} only, so the global
 * {@code upwards_knockback} config can stay disabled for other weapons.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class HoeUpwardKnockbackHandler {

    public static final TagKey<Item> HOES = TagKey.create(
            Registries.ITEM,
            ResourceLocation.withDefaultNamespace("hoes")
    );

    private HoeUpwardKnockbackHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHoeKnockback(LivingKnockBackEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.onGround() || victim.isInWater()) {
            return;
        }

        LivingEntity attacker = victim.getKillCredit();
        if (attacker == null) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty() || !weapon.is(HOES)) {
            return;
        }

        float strength = event.getStrength()
                * (float) (1.0D - victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        event.setStrength(strength);

        Vec3 deltaMovement = victim.getDeltaMovement();
        victim.setDeltaMovement(
                deltaMovement.x,
                Math.min(0.4D, deltaMovement.y / 2.0D + strength),
                deltaMovement.z
        );
    }
}
