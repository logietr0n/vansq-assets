package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Hominid applies Paranoia while a Vampire is merely nearby. Restrict it to injured players
 * a Vampire is stalking or attacking, and strip food/item sources.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class HominidParanoiaHandler {

    private static final ResourceLocation PARANOIA_ID =
            ResourceLocation.fromNamespaceAndPath("hominid", "paranoia");
    private static final ResourceLocation VAMPIRE_ID =
            ResourceLocation.fromNamespaceAndPath("hominid", "vampire");
    /** Match Vampire {@code FollowPlayerGoal} followDistance. */
    private static final double VAMPIRE_SEARCH_RANGE = 120.0D;
    private static final int PARANOIA_DURATION = 80;

    private HominidParanoiaHandler() {
    }

    @SubscribeEvent
    public static void onParanoiaApplicable(MobEffectEvent.Applicable event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (!isParanoia(instance)) {
            return;
        }
        if (!(event.getEntity() instanceof Player player) || !shouldHaveParanoia(player)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        Holder<MobEffect> paranoia = BuiltInRegistries.MOB_EFFECT.getHolder(PARANOIA_ID).orElse(null);
        if (paranoia == null) {
            return;
        }
        if (shouldHaveParanoia(player)) {
            MobEffectInstance current = player.getEffect(paranoia);
            if (current == null || current.getDuration() < 40) {
                player.addEffect(new MobEffectInstance(paranoia, PARANOIA_DURATION, 0, false, true));
            }
        } else if (player.hasEffect(paranoia)) {
            player.removeEffect(paranoia);
        }
    }

    private static boolean shouldHaveParanoia(Player player) {
        if (player.getAbilities().instabuild || player.isSpectator()) {
            return false;
        }
        if (player.getHealth() >= player.getMaxHealth()) {
            return false;
        }
        return isTargetedByVampire(player);
    }

    private static boolean isTargetedByVampire(Player player) {
        EntityType<?> vampireType = BuiltInRegistries.ENTITY_TYPE.getOptional(VAMPIRE_ID).orElse(null);
        if (vampireType == null) {
            return false;
        }
        AABB search = player.getBoundingBox().inflate(VAMPIRE_SEARCH_RANGE);
        for (Entity entity : player.level().getEntities(vampireType, search, Entity::isAlive)) {
            if (!(entity instanceof Mob mob)) {
                continue;
            }
            LivingEntity target = mob.getTarget();
            if (target == player) {
                return true;
            }
            // Stalk/follow never calls setTarget; it only runs while getTarget is empty
            // and the nearest player is within followDistance.
            if (target == null
                    && mob.distanceToSqr(player) <= VAMPIRE_SEARCH_RANGE * VAMPIRE_SEARCH_RANGE
                    && player.level().getNearestPlayer(mob, VAMPIRE_SEARCH_RANGE) == player) {
                return true;
            }
        }
        return false;
    }

    private static boolean isParanoia(MobEffectInstance instance) {
        if (instance == null) {
            return false;
        }
        return PARANOIA_ID.equals(BuiltInRegistries.MOB_EFFECT.getKey(instance.getEffect().value()));
    }
}
