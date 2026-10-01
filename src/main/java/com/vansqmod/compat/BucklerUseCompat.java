package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import tallestred.piglinproliferation.common.items.BucklerItem;

/**
 * Keeps the Piglin Proliferation 10-tick charge windup intact when other
 * combat mods shorten or cancel {@code UseAnim.BLOCK} starts.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class BucklerUseCompat {

    private BucklerUseCompat() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void prepareBucklerStart(LivingEntityUseItemEvent.Start event) {
        restoreBucklerStart(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void finishBucklerStart(LivingEntityUseItemEvent.Start event) {
        restoreBucklerStart(event);
    }

    private static void restoreBucklerStart(LivingEntityUseItemEvent.Start event) {
        if (!ModList.get().isLoaded("piglinproliferation")) {
            return;
        }
        ItemStack stack = event.getItem();
        if (!(stack.getItem() instanceof BucklerItem)) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (BucklerItem.isReady(stack) && BucklerItem.getChargeTicks(stack) <= 0) {
            BucklerItem.setReady(stack, false);
        }
        if (!BucklerItem.isReady(stack)) {
            BucklerItem.CHARGE_SPEED_BOOST.removeModifier(entity);
            BucklerItem.CHARGE_JUMP_PREVENTION.removeModifier(entity);
            BucklerItem.INCREASED_KNOCKBACK_RESISTANCE.removeModifier(entity);
            BucklerItem.TURNING_SPEED_REDUCTION.removeModifier(entity);
        }
        event.setCanceled(false);
        int duration = stack.getUseDuration(entity);
        if (event.getDuration() != duration) {
            event.setDuration(duration);
        }
    }
}
