package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Verdant and Bogged shoot poison arrows. Poison heals undead, so a stray shot must
 * not make the undead retaliate against the shooter.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class PoisonSkeletonUndeadTruce {

    private static final ThreadLocal<Boolean> FRIENDLY_HIT = ThreadLocal.withInitial(() -> false);

    private PoisonSkeletonUndeadTruce() {
    }

    public static boolean isPoisonSkeleton(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (entity.getType() == EntityType.BOGGED) {
            return true;
        }
        return VerdantCombat.isVerdant(entity);
    }

    public static boolean isUndead(LivingEntity living) {
        return living.getType().is(EntityTypeTags.UNDEAD) || living.isInvertedHealAndHarm();
    }

    public static boolean isFriendlyPoisonHit(AbstractArrow arrow, Entity hit) {
        if (!(hit instanceof LivingEntity living) || living.level().isClientSide()) {
            return false;
        }
        return isUndead(living) && isPoisonSkeleton(arrow.getOwner());
    }

    public static boolean isPoisonSkeletonArrow(DamageSource source) {
        if (source == null || !(source.getDirectEntity() instanceof AbstractArrow)) {
            return false;
        }
        return isPoisonSkeleton(source.getEntity());
    }

    public static void beginFriendlyHit() {
        FRIENDLY_HIT.set(true);
    }

    public static void endFriendlyHit() {
        FRIENDLY_HIT.remove();
    }

    public static void forgetShooter(LivingEntity victim, Entity shooter) {
        if (victim.getLastHurtByMob() == shooter) {
            victim.setLastHurtByMob(null);
        }
        if (victim instanceof Mob mob && mob.getTarget() == shooter) {
            mob.setTarget(null);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!isUndead(event.getEntity()) || !isPoisonSkeletonArrow(event.getSource())) {
            return;
        }
        event.setCanceled(true);
        forgetShooter(event.getEntity(), event.getSource().getEntity());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity attacker = event.getEntity();
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget == null || !isUndead(attacker) || !isPoisonSkeleton(newTarget)) {
            return;
        }
        if (FRIENDLY_HIT.get() || isPoisonSkeletonArrow(attacker.getLastDamageSource())) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) {
            return;
        }
        if (!isUndead(mob) || !isPoisonSkeletonArrow(mob.getLastDamageSource())) {
            return;
        }
        Entity shooter = mob.getLastDamageSource().getEntity();
        forgetShooter(mob, shooter);
    }
}
