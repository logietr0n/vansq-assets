package com.vansqmod.entity;

import com.vansqmod.VansqMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * Baby skeletons keep adult attributes except max health, which is halved.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class SkeletonBabyAttributes {

    private static final ResourceLocation HALF_HEALTH_ID =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "skeleton_baby_half_health");

    private static final AttributeModifier HALF_HEALTH = new AttributeModifier(
            HALF_HEALTH_ID, -0.5D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private SkeletonBabyAttributes() {
    }

    public static void apply(LivingEntity entity) {
        if (!SkeletonBabies.isFamily(entity)) {
            return;
        }
        setModifier(entity, Attributes.MAX_HEALTH, HALF_HEALTH, SkeletonBabies.isMarkedBaby(entity));
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }

    private static void setModifier(
            LivingEntity entity,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            AttributeModifier modifier,
            boolean enabled
    ) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(modifier.id());
        if (enabled) {
            instance.addTransientModifier(modifier);
        }
    }

    /**
     * Some spawn paths add the entity before {@code finalizeSpawn}, or skip the
     * AbstractSkeleton mixin entirely. Roll here so chaos / the 10% pair chance
     * still apply. {@link SkeletonBabies#tryReplaceWithBabyPair} is idempotent.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (event.isCanceled() || event.isSpawnCancelled() || event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof Mob mob && SkeletonBabies.rollsNaturalBaby(mob)) {
            SkeletonBabies.tryReplaceWithBabyPair(mob, event.getSpawnType());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof Mob mob) || !SkeletonBabies.rollsNaturalBaby(mob)) {
            return;
        }
        if (SkeletonBabies.hasPendingBaby(mob)) {
            SkeletonBabies.applyPendingBaby(mob);
            if (SkeletonBabies.isMarkedBaby(mob)) {
                SkeletonBabies.prepareBaby(mob);
            }
            apply(mob);
            return;
        }
        if (SkeletonBabies.isMarkedBaby(mob)) {
            SkeletonBabies.prepareBaby(mob);
        } else if (!SkeletonBabies.isBabyRollDone(mob)) {
            SkeletonBabies.tryReplaceWithBabyPair(mob, MobSpawnType.NATURAL);
        } else {
            SkeletonBabies.ensureAdultBow(mob);
        }
        apply(mob);
    }

    @SubscribeEvent
    public static void extraBone(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (!SkeletonBabies.isMarkedBaby(entity) || entity.getType() != EntityType.SKELETON) {
            return;
        }
        event.getDrops().add(new ItemEntity(
                entity.level(),
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                new ItemStack(Items.BONE)));
    }
}
