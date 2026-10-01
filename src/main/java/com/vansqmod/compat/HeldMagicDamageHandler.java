package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Caverns &amp; Chasms applies {@code magic_damage} as a follow-up in
 * {@code LivingDamageEvent.Post}. That never runs when the melee hit is 0 physical
 * (silver knife), and it also skips when a mob never received the attribute instance
 * from the held item. Those hits are converted into the held magic damage here.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class HeldMagicDamageHandler {

    private static final ResourceLocation CC_MAGIC_DAMAGE =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "magic_damage");
    private static final TagKey<net.minecraft.world.entity.EntityType<?>> SILVER_HURTS_EXTRA = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "silver_hurts_extra_types")
    );
    private static final ThreadLocal<Boolean> REENTRANT = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private HeldMagicDamageHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (Boolean.TRUE.equals(REENTRANT.get()) || event.getEntity().level().isClientSide) {
            return;
        }
        if (event.getAmount() > 0.0F) {
            return;
        }
        DamageSource source = event.getSource();
        if (source.is(Tags.DamageTypes.IS_MAGIC) || !(source.getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        if (attacker == event.getEntity()) {
            return;
        }
        float magic = magicDamageOf(attacker);
        if (magic <= 0.0F) {
            return;
        }
        event.setCanceled(true);
        dealMagic(attacker, event.getEntity(), magic);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onDamagePost(LivingDamageEvent.Post event) {
        if (Boolean.TRUE.equals(REENTRANT.get()) || event.getEntity().level().isClientSide) {
            return;
        }
        DamageSource source = event.getSource();
        if (source.is(Tags.DamageTypes.IS_MAGIC) || !(source.getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        if (attacker == event.getEntity()) {
            return;
        }
        Holder<Attribute> magicAttr = BuiltInRegistries.ATTRIBUTE.getHolder(CC_MAGIC_DAMAGE).orElse(null);
        if (magicAttr == null) {
            return;
        }
        AttributeInstance instance = attacker.getAttribute(magicAttr);
        if (instance != null && instance.getValue() > 0.0D) {
            // C&C already applied the follow-up magic hit.
            return;
        }
        float magic = magicFromHeldItem(attacker, magicAttr);
        if (magic <= 0.0F) {
            return;
        }
        dealMagic(attacker, event.getEntity(), magic);
    }

    private static void dealMagic(LivingEntity attacker, LivingEntity target, float magic) {
        if (target.getType().is(SILVER_HURTS_EXTRA)) {
            magic *= 3.0F;
        }
        target.invulnerableTime = 0;
        REENTRANT.set(Boolean.TRUE);
        try {
            target.hurt(target.damageSources().indirectMagic(attacker, attacker), magic);
        } finally {
            REENTRANT.set(Boolean.FALSE);
        }
    }

    private static float magicDamageOf(LivingEntity attacker) {
        Holder<Attribute> magic = BuiltInRegistries.ATTRIBUTE.getHolder(CC_MAGIC_DAMAGE).orElse(null);
        if (magic == null) {
            return 0.0F;
        }
        AttributeInstance instance = attacker.getAttribute(magic);
        if (instance != null && instance.getValue() > 0.0D) {
            return (float) instance.getValue();
        }
        return magicFromHeldItem(attacker, magic);
    }

    private static float magicFromHeldItem(LivingEntity attacker, Holder<Attribute> magic) {
        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty()) {
            return 0.0F;
        }
        double[] total = {0.0D};
        weapon.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(magic) && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                total[0] += modifier.amount();
            }
        });
        return (float) Math.max(0.0D, total[0]);
    }
}
