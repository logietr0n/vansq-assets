package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

/**
 * Verdant combat tweaks. HP/speed/scale/fall live in {@link PackEntityAttributes};
 * burst firing and leaps live on {@code VerdantCombatMixin}.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class VerdantCombat {

    public static final ResourceLocation VERDANT_ID =
            ResourceLocation.fromNamespaceAndPath("variantsandventures", "verdant");

    public static final double ARROW_DAMAGE_SCALE = 0.3D;
    public static final float ARROW_KNOCKBACK_SCALE = 0.25F;
    public static final int BURST_EXTRA_SHOTS = 2;
    /** 0.3 seconds at 20 ticks/second. */
    public static final int BURST_INTERVAL_TICKS = 6;
    /** Same as vanilla Bogged {@code getAttackInterval()}. */
    public static final int ATTACK_INTERVAL = 70;
    /** Same as vanilla Bogged {@code getHardAttackInterval()}. */
    public static final int HARD_ATTACK_INTERVAL = 50;
    /** Jump Boost II extra from {@code LivingEntity#getJumpBoostPower()}. */
    public static final double JUMP_BOOST_II_EXTRA = 0.2D;
    public static final int CHASE_JUMP_COOLDOWN = 4;
    private static final double SHOT_LEAP_HORIZONTAL = 0.22D;
    private static final double CHASE_LEAP_HORIZONTAL = 0.32D;

    private static final ThreadLocal<Integer> ARROW_HIT_DEPTH = ThreadLocal.withInitial(() -> 0);

    private VerdantCombat() {
    }

    public static boolean isVerdant(Entity entity) {
        return entity != null && VERDANT_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }

    public static void leapOnBowShot(AbstractSkeleton verdant, LivingEntity target) {
        leapAlongPath(verdant, JUMP_BOOST_II_EXTRA, SHOT_LEAP_HORIZONTAL);
    }

    public static boolean tryChaseJump(AbstractSkeleton verdant) {
        LivingEntity target = verdant.getTarget();
        if (target == null || !target.isAlive() || !shouldChaseJump(verdant)) {
            return false;
        }
        return leapTowards(verdant, target, 0.0D, CHASE_LEAP_HORIZONTAL);
    }

    private static boolean shouldChaseJump(AbstractSkeleton verdant) {
        if (holdsRangedTool(verdant)) {
            return false;
        }
        return verdant.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrapped -> wrapped.isRunning() && wrapped.getGoal() instanceof MeleeAttackGoal);
    }

    private static boolean holdsRangedTool(LivingEntity entity) {
        return isRangedTool(entity.getMainHandItem()) || isRangedTool(entity.getOffhandItem());
    }

    private static boolean isRangedTool(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ProjectileWeaponItem;
    }

    private static boolean canLeap(LivingEntity verdant) {
        return !verdant.level().isClientSide() && verdant.onGround() && !verdant.isPassenger() && !verdant.isInWater();
    }

    private static boolean leapAlongPath(Mob verdant, double extraY, double horizontal) {
        if (!canLeap(verdant)) {
            return false;
        }
        Vec3 heading = pathHeading(verdant);
        verdant.jumpFromGround();
        Vec3 motion = verdant.getDeltaMovement();
        double y = motion.y + extraY;
        if (heading != null && heading.horizontalDistanceSqr() > 1.0E-8D) {
            Vec3 dir = new Vec3(heading.x, 0.0D, heading.z).normalize();
            double speed = Math.max(motion.horizontalDistance(), horizontal);
            verdant.setDeltaMovement(dir.x * speed, y, dir.z * speed);
        } else {
            verdant.setDeltaMovement(motion.x, y, motion.z);
        }
        return true;
    }

    private static Vec3 pathHeading(Mob mob) {
        var path = mob.getNavigation().getPath();
        if (path != null && !path.isDone()) {
            Vec3 next = path.getNextEntityPos(mob);
            double dx = next.x - mob.getX();
            double dz = next.z - mob.getZ();
            if (dx * dx + dz * dz > 1.0E-6D) {
                return new Vec3(dx, 0.0D, dz);
            }
        }
        var move = mob.getMoveControl();
        if (move.hasWanted()) {
            double dx = move.getWantedX() - mob.getX();
            double dz = move.getWantedZ() - mob.getZ();
            if (dx * dx + dz * dz > 1.0E-6D) {
                return new Vec3(dx, 0.0D, dz);
            }
        }
        Vec3 vel = mob.getDeltaMovement();
        if (vel.horizontalDistanceSqr() > 1.0E-6D) {
            return new Vec3(vel.x, 0.0D, vel.z);
        }
        return null;
    }

    private static boolean leapTowards(LivingEntity verdant, LivingEntity target, double extraY, double horizontal) {
        if (!canLeap(verdant)) {
            return false;
        }
        verdant.jumpFromGround();
        Vec3 motion = verdant.getDeltaMovement();
        double y = motion.y + extraY;
        double dx = target.getX() - verdant.getX();
        double dz = target.getZ() - verdant.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > 1.0E-4D && horizontal > 0.0D) {
            double scale = horizontal / dist;
            verdant.setDeltaMovement(motion.x + dx * scale, y, motion.z + dz * scale);
        } else {
            verdant.setDeltaMovement(motion.x, y, motion.z);
        }
        return true;
    }

    public static boolean isVerdantArrow(Entity entity) {
        return entity instanceof AbstractArrow arrow && isVerdant(arrow.getOwner());
    }

    public static void beginArrowHit(AbstractArrow arrow) {
        if (isVerdant(arrow.getOwner())) {
            ARROW_HIT_DEPTH.set(ARROW_HIT_DEPTH.get() + 1);
        }
    }

    public static void endArrowHit(AbstractArrow arrow) {
        if (!isVerdant(arrow.getOwner())) {
            return;
        }
        int depth = ARROW_HIT_DEPTH.get() - 1;
        if (depth <= 0) {
            ARROW_HIT_DEPTH.remove();
        } else {
            ARROW_HIT_DEPTH.set(depth);
        }
    }

    public static boolean isArrowHitActive() {
        return ARROW_HIT_DEPTH.get() > 0;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onArrowKnockback(LivingKnockBackEvent event) {
        if (!isVerdantArrowKnockback(event)) {
            return;
        }
        event.setStrength(event.getStrength() * ARROW_KNOCKBACK_SCALE);
    }

    private static boolean isVerdantArrowKnockback(LivingKnockBackEvent event) {
        if (isArrowHitActive()) {
            return true;
        }
        DamageSource source = event.getEntity().getLastDamageSource();
        return source != null && isVerdantArrow(source.getDirectEntity());
    }
}
