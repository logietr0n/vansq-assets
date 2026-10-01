package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import com.vansqmod.mixin.PlayerEnchantedDamageInvoker;
import com.vansqmod.registry.ModItemTags;
import com.vansqmod.registry.ModSoundEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.event.entity.player.SweepAttackEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Grants Combat Nouveau-style sweeping (no Sweeping Edge required) only to
 * {@link ModItemTags#SWEEPING}.
 * <p>
 * Knives use tooltip attack damage only and may sweep while airborne. Scythes match a fully charged primary hit:
 * live attack-damage attribute + per-target enchant bonuses (Smite, Sharpness, etc.)
 * and post-attack enchant effects.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class SweepingTagHandler {

    private static long claimGameTime = Long.MIN_VALUE;
    private static final Map<UUID, Set<Integer>> CLAIMED_HITS = new HashMap<>();
    private static final Map<UUID, Long> CLAIMED_FX = new HashMap<>();
    private static final ThreadLocal<Boolean> REENTRANT = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /** Shared air-sweep SFX. Scythes can use a different pitch later without a new sound. */
    private static final float SWEEP_PITCH = 1.0F;
    private static final float SCYTHE_SWEEP_PITCH = 0.6F;

    private SweepingTagHandler() {
    }

    public static boolean isSweepingWeapon(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModItemTags.SWEEPING);
    }

    public static boolean isKnifeWeapon(ItemStack stack) {
        return !stack.isEmpty() && stack.is(KnifeNoKnockbackHandler.TOOLS_KNIFE);
    }

    public static float sweepPitch(ItemStack weapon) {
        return weapon.is(ModItemTags.SCYTHE) ? SCYTHE_SWEEP_PITCH : SWEEP_PITCH;
    }

    public static boolean meetsSweepStance(Player player) {
        if (player.getAttackStrengthScale(0.5F) <= 0.9F) {
            return false;
        }
        if (player.isSprinting()) {
            return false;
        }
        // Knives can sweep while jumping / falling; scythes and other sweeping items cannot.
        if (!player.onGround() && !isKnifeWeapon(player.getMainHandItem())) {
            return false;
        }
        double walked = player.walkDist - player.walkDistO;
        return walked < player.getSpeed();
    }

    public static boolean hasSweepingEdge(Player player) {
        return player.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) > 0.0F;
    }

    /**
     * Attack damage shown for the held weapon (player base 1.0 + item mainhand ADD_VALUE
     * modifiers). Copper knife → {@code 1.0 + (-0.75 + 0.5) = 0.75}.
     * Falls back to the live attack-damage attribute if the stack has no item modifiers.
     */
    public static float getWeaponAttackDamage(Player player, ItemStack weapon) {
        double bonus = 0.0D;
        boolean found = false;
        ItemAttributeModifiers modifiers = weapon.getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY
        );
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (!entry.attribute().is(Attributes.ATTACK_DAMAGE)) {
                continue;
            }
            EquipmentSlotGroup slot = entry.slot();
            if (slot != EquipmentSlotGroup.MAINHAND
                    && slot != EquipmentSlotGroup.ANY
                    && slot != EquipmentSlotGroup.HAND) {
                continue;
            }
            AttributeModifier modifier = entry.modifier();
            if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                bonus += modifier.amount();
                found = true;
            }
        }
        if (!found) {
            return (float) Math.max(0.0D, player.getAttributeValue(Attributes.ATTACK_DAMAGE));
        }
        return (float) Math.max(0.0D, 1.0D + bonus);
    }

    @Nullable
    public static Entity findLookTarget(Player player) {
        double range = player.entityInteractionRange();
        Vec3 from = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 to = from.add(look.scale(range));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player,
                from,
                to,
                searchBox,
                entity -> entity instanceof LivingEntity
                        && entity.isPickable()
                        && !entity.isSpectator()
                        && entity.isAttackable(),
                range * range
        );
        return hit != null ? hit.getEntity() : null;
    }

    private static void resetClaimsIfNeeded(Player player) {
        long time = player.level().getGameTime();
        if (time != claimGameTime) {
            CLAIMED_HITS.clear();
            CLAIMED_FX.clear();
            claimGameTime = time;
        }
    }

    public static boolean claimSweepHit(Player player, LivingEntity target) {
        resetClaimsIfNeeded(player);
        return CLAIMED_HITS
                .computeIfAbsent(player.getUUID(), id -> new HashSet<>())
                .add(target.getId());
    }

    public static boolean claimSweepFx(Player player) {
        resetClaimsIfNeeded(player);
        Long previous = CLAIMED_FX.put(player.getUUID(), claimGameTime);
        return previous == null || previous != claimGameTime;
    }

    /**
     * Vanilla sweep uses a 3-block radius ({@code distanceToSqr < 9}). Expand the query box
     * when {@link Player#entityInteractionRange()} is longer so far targets are found.
     */
    private static AABB expandSweepHitBox(Player player, AABB hitBox) {
        double extra = player.entityInteractionRange() - 3.0D;
        if (extra <= 0.0D) {
            return hitBox;
        }
        return hitBox.inflate(extra);
    }

    /**
     * Damage one sweep target. Scythes mirror {@link Player#attack} damage
     * (attribute + enchantments + charge scale + post-attack effects); other
     * sweeping weapons keep tooltip damage only.
     */
    private static void hurtSweepTarget(
            Player player,
            LivingEntity target,
            ItemStack weapon,
            DamageSource damageSource,
            float tooltipDamage,
            float chargeScale
    ) {
        target.knockback(
                0.4F,
                Mth.sin(player.getYRot() * ((float) Math.PI / 180.0F)),
                -Mth.cos(player.getYRot() * ((float) Math.PI / 180.0F))
        );

        if (!weapon.is(ModItemTags.SCYTHE)) {
            target.hurt(damageSource, tooltipDamage);
            return;
        }

        // Same order as Player.attack: enchant on full attribute damage, then charge scale.
        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float enchanted = ((PlayerEnchantedDamageInvoker) player)
                .vansqmod$invokeGetEnchantedDamage(target, baseDamage, damageSource);
        enchanted *= 0.2F + chargeScale * chargeScale * 0.8F;
        target.hurt(damageSource, enchanted);
        if (player.level() instanceof ServerLevel serverLevel) {
            EnchantmentHelper.doPostAttackEffects(serverLevel, target, damageSource);
        }
    }

    /**
     * Apply sweep damage to entities in {@code hitBox}.
     * Sweep radius matches {@link Player#entityInteractionRange()}.
     *
     * @param excluded entity that must not take sweep damage (melee / look target)
     */
    public static void performFullDamageSweep(Player player, AABB hitBox, @Nullable Entity excluded) {
        if (player.level().isClientSide || Boolean.TRUE.equals(REENTRANT.get())) {
            return;
        }

        ItemStack weapon = player.getMainHandItem();
        boolean scythe = weapon.is(ModItemTags.SCYTHE);
        float tooltipDamage = getWeaponAttackDamage(player, weapon);
        float attributeDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if ((scythe ? attributeDamage : tooltipDamage) <= 0.0F) {
            return;
        }

        float chargeScale = player.getAttackStrengthScale(0.5F);
        double range = player.entityInteractionRange();
        double rangeSq = range * range;
        AABB sweepBox = expandSweepHitBox(player, hitBox);

        DamageSource damageSource = player.damageSources().playerAttack(player);
        REENTRANT.set(Boolean.TRUE);
        try {
            for (LivingEntity livingEntity : player.level().getEntitiesOfClass(LivingEntity.class, sweepBox)) {
                if (livingEntity == player
                        || livingEntity == excluded
                        || (excluded != null && livingEntity.getId() == excluded.getId())
                        || player.isAlliedTo(livingEntity)
                        || (livingEntity instanceof ArmorStand armorStand && armorStand.isMarker())
                        || player.distanceToSqr(livingEntity) >= rangeSq
                        || !claimSweepHit(player, livingEntity)) {
                    continue;
                }

                hurtSweepTarget(player, livingEntity, weapon, damageSource, tooltipDamage, chargeScale);
            }

            if (claimSweepFx(player)) {
                playSweepFx(player, weapon);
            }
        } finally {
            REENTRANT.set(Boolean.FALSE);
        }
    }

    /**
     * Shared sweep sound for {@code #vansqmod:sweeping}; slash particle is vanilla sweep_attack.
     * Combat Nouveau air-sweeps run on the server only, so the attacker must not be excluded.
     */
    public static void playSweepFx(Player player, ItemStack weapon) {
        float pitch = sweepPitch(weapon);
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSoundEvents.SWEEP.get(),
                player.getSoundSource(),
                1.0F,
                pitch
        );
        player.sweepAttack();
    }

    public static void performFullDamageSweep(Player player, Entity primaryTarget) {
        ItemStack weapon = player.getMainHandItem();
        AABB hitBox = weapon.getSweepHitBox(player, primaryTarget);
        performFullDamageSweep(player, hitBox, primaryTarget);
    }

    /**
     * Air-sweep when Combat Nouveau is not present to send its sweep packet.
     * Hitbox matches CN: weapon sweep box moved 2 blocks forward; look-target excluded.
     */
    public static void performAirSweep(Player player) {
        if (player.level().isClientSide
                || !isSweepingWeapon(player.getMainHandItem())
                || !meetsSweepStance(player)
                || player.getAttackStrengthScale(0.5F) < 1.0F) {
            return;
        }

        ItemStack weapon = player.getMainHandItem();
        double forwardX = -Mth.sin(player.getYRot() * ((float) Math.PI / 180.0F)) * 2.0D;
        double forwardZ = Mth.cos(player.getYRot() * ((float) Math.PI / 180.0F)) * 2.0D;
        AABB hitBox = weapon.getSweepHitBox(player, player).move(forwardX, 0.0D, forwardZ);
        performFullDamageSweep(player, hitBox, findLookTarget(player));
        // Same as Combat Nouveau client air-sweep: consume the charge so packets cannot spam.
        player.resetAttackStrengthTicker();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onSweepAttack(SweepAttackEvent event) {
        Player player = event.getEntity();
        ItemStack weapon = player.getMainHandItem();

        if (!isSweepingWeapon(weapon)) {
            if (event.isSweeping() && !hasSweepingEdge(player)) {
                event.setSweeping(false);
            }
            return;
        }

        // Prevent hurt→attack→sweep re-entrancy from stacking damage.
        if (Boolean.TRUE.equals(REENTRANT.get())) {
            event.setSweeping(false);
            event.setCanceled(true);
            return;
        }

        if (!meetsSweepStance(player)) {
            event.setSweeping(false);
            return;
        }

        event.setSweeping(false);
        event.setCanceled(true);
        if (!player.level().isClientSide) {
            performFullDamageSweep(player, event.getTarget());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onVanillaSweepSound(PlayLevelSoundEvent.AtPosition event) {
        replaceVanillaSweepSound(event, findSweepingPlayer(event.getLevel(), event.getPosition()));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onVanillaSweepSound(PlayLevelSoundEvent.AtEntity event) {
        Entity entity = event.getEntity();
        Player player = entity instanceof Player p ? p : null;
        if (player == null || !isSweepingWeapon(player.getMainHandItem())) {
            return;
        }
        replaceVanillaSweepSound(event, player);
    }

    private static void replaceVanillaSweepSound(PlayLevelSoundEvent event, @Nullable Player player) {
        if (player == null || !isVanillaSweepSound(event.getSound())) {
            return;
        }
        event.setSound(ModSoundEvents.SWEEP);
        event.setNewPitch(sweepPitch(player.getMainHandItem()));
    }

    private static boolean isVanillaSweepSound(@Nullable Holder<SoundEvent> sound) {
        return sound != null && sound.value() == SoundEvents.PLAYER_ATTACK_SWEEP;
    }

    @Nullable
    private static Player findSweepingPlayer(Level level, Vec3 pos) {
        Player best = null;
        double bestDist = 0.25D;
        for (Player player : level.players()) {
            if (!isSweepingWeapon(player.getMainHandItem())) {
                continue;
            }
            double dist = player.distanceToSqr(pos);
            if (dist < bestDist) {
                bestDist = dist;
                best = player;
            }
        }
        return best;
    }
}
