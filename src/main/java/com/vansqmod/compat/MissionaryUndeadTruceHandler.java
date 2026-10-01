package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Undead never aggro the Missionary. Missionary attacks skip undead so shots pass through its own summons.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class MissionaryUndeadTruceHandler {

    private static final ResourceLocation MISSIONER =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "missioner");
    private static final ResourceLocation RESTLESS_SPIRIT =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "restless_spirit");

    private MissionaryUndeadTruceHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity attacker = event.getEntity();
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget == null || !isUndead(attacker) || isMissioner(attacker)) {
            return;
        }
        if (isMissioner(newTarget)) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }
        if (isMissioner(livingAttacker) && isUndead(victim)) {
            event.setCanceled(true);
            return;
        }
        if (isUndead(livingAttacker) && isMissioner(victim)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) {
            return;
        }
        if (!isUndead(mob) || isMissioner(mob)) {
            return;
        }
        LivingEntity target = mob.getTarget();
        if (target != null && isMissioner(target)) {
            mob.setTarget(null);
        }
    }

    private static boolean isMissioner(Entity entity) {
        if (entity == null) {
            return false;
        }
        return MISSIONER.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }

    private static boolean isUndead(LivingEntity living) {
        if (living.getType().is(EntityTypeTags.UNDEAD) || living.isInvertedHealAndHarm()) {
            return true;
        }
        return RESTLESS_SPIRIT.equals(BuiltInRegistries.ENTITY_TYPE.getKey(living.getType()));
    }
}
