package com.vansqmod.entity;

import com.vansqmod.VansqMod;
import com.vansqmod.registry.ModEntityTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

/**
 * Tagged mobs keep monster spawn/AI properties, but non-hostile features
 * (golems, guards, and similar) should not treat them as monsters to hunt.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class NotMonsterHandler {

    private NotMonsterHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget == null || !newTarget.getType().is(ModEntityTags.NOT_MONSTERS)) {
            return;
        }
        LivingEntity attacker = event.getEntity();
        if (attacker instanceof Player || attacker instanceof Enemy) {
            return;
        }
        event.setNewAboutToBeSetTarget(null);
    }
}
