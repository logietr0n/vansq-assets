package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import com.vansqmod.registry.ModAttributes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Turns a fraction of attack damage dealt by a silver-crown wearer into Caverns &amp; Chasms
 * style magic damage. The physical hit is reduced by that fraction so the total stays the same
 * before magic protection. This does not use {@code caverns_and_chasms:magic_damage}, which
 * would add a flat extra hit on top.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class CrownMagicConversion {

    private static final TagKey<net.minecraft.world.entity.EntityType<?>> SILVER_HURTS_EXTRA = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "silver_hurts_extra_types")
    );
    private static final ThreadLocal<Float> PENDING = ThreadLocal.withInitial(() -> 0.0F);
    private static final ThreadLocal<Boolean> REENTRANT = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private CrownMagicConversion() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamagePre(LivingDamageEvent.Pre event) {
        if (Boolean.TRUE.equals(REENTRANT.get()) || event.getEntity().level().isClientSide) {
            return;
        }
        DamageSource source = event.getSource();
        if (source.is(Tags.DamageTypes.IS_MAGIC)) {
            return;
        }
        PENDING.set(0.0F);
        if (!(source.getEntity() instanceof LivingEntity attacker) || attacker == event.getEntity()) {
            return;
        }
        if (!isAttack(source, attacker)) {
            return;
        }
        AttributeInstance conversion = attacker.getAttribute(ModAttributes.MAGIC_CONVERSION);
        if (conversion == null || conversion.getValue() <= 0.0D) {
            return;
        }
        float dealt = event.getNewDamage();
        if (dealt <= 0.0F) {
            return;
        }
        float magic = dealt * (float) conversion.getValue();
        event.setNewDamage(dealt - magic);
        PENDING.set(magic);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamagePost(LivingDamageEvent.Post event) {
        if (Boolean.TRUE.equals(REENTRANT.get()) || event.getEntity().level().isClientSide) {
            return;
        }
        DamageSource source = event.getSource();
        if (source.is(Tags.DamageTypes.IS_MAGIC)) {
            return;
        }
        float magic = PENDING.get();
        PENDING.set(0.0F);
        if (magic <= 0.0F || !(source.getEntity() instanceof LivingEntity attacker) || attacker == event.getEntity()) {
            return;
        }
        LivingEntity target = event.getEntity();
        if (target.getType().is(SILVER_HURTS_EXTRA)) {
            magic *= 3.0F;
        }
        target.invulnerableTime = 0;
        REENTRANT.set(Boolean.TRUE);
        try {
            target.hurt(target.damageSources().indirectMagic(attacker, attacker), magic);
            causeMagicDamageEffects(attacker, target);
        } finally {
            REENTRANT.set(Boolean.FALSE);
        }
    }

    private static boolean isAttack(DamageSource source, LivingEntity attacker) {
        Entity direct = source.getDirectEntity();
        return direct == attacker || direct instanceof Projectile;
    }

    private static void causeMagicDamageEffects(LivingEntity attacker, LivingEntity target) {
        try {
            Class.forName("com.teamabnormals.caverns_and_chasms.common.item.silver.SilverItem")
                    .getMethod("causeMagicDamageEffects", LivingEntity.class, LivingEntity.class)
                    .invoke(null, attacker, target);
        } catch (ReflectiveOperationException ignored) {
            // Caverns & Chasms is optional; the magic damage itself is vanilla indirect magic.
        }
    }
}
