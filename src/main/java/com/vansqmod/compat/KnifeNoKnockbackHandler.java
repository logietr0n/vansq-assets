package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

/**
 * All items in {@code #c:tools/knife} deal no melee knockback, including any added to the tag via datapacks.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class KnifeNoKnockbackHandler {

    public static final TagKey<Item> TOOLS_KNIFE = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("c", "tools/knife")
    );

    private KnifeNoKnockbackHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onKnifeKnockback(LivingKnockBackEvent event) {
        LivingEntity attacker = event.getEntity().getKillCredit();
        if (attacker == null) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        if (!weapon.isEmpty() && weapon.is(TOOLS_KNIFE)) {
            event.setStrength(0.0F);
        }
    }
}
