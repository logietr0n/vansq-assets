package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import com.vansqmod.debug.VansqDebugState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MultiplayerBosses only applies its health modifier once on spawn. While a boss
 * has not taken damage, keep that modifier and current HP in sync with nearby
 * player count so a 300/300 Wither becomes 450/450, not 300/450.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class MultiplayerBossHealthRescale {

    private static final ResourceLocation MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("multiplayerbosses", "player_count_scalar");
    private static final String PLAYER_COUNT_NBT = "lh_multi";
    private static final String LOCKED_NBT = "vansq_mpb_hp_locked";
    private static final String LAST_HEALTH_NBT = "vansq_mpb_last_health";
    private static final int PERIOD_TICKS = 5;
    private static final float HEALTH_SLOP = 0.5F;

    private static boolean resolved;
    private static Object config;
    private static Method isBossMethod;
    private static Method shouldScale;
    private static Method useProximity;
    private static Method proximityRange;
    private static Method perPlayer;
    private static Method flatHealth;

    private MultiplayerBossHealthRescale() {
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living) || living.level().isClientSide()) {
            return;
        }
        if (living.tickCount % PERIOD_TICKS != 0 || !living.isAlive()) {
            return;
        }
        if (!ModList.get().isLoaded("multiplayerbosses")) {
            return;
        }
        if (!resolve() || !scaleHealthEnabled() || !isBoss(living)) {
            return;
        }
        apply(living);
    }

    private static void apply(LivingEntity living) {
        AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }
        CompoundTag data = living.getPersistentData();
        float oldMax = (float) maxHealth.getValue();
        float oldHealth = living.getHealth();
        boolean charging = isCharging(living);
        if (data.getBoolean(LOCKED_NBT) && !charging) {
            return;
        }
        if (!charging && hasTakenDamage(data, oldHealth)) {
            data.putBoolean(LOCKED_NBT, true);
            return;
        }

        double playerCount = countPlayers(living);
        double flat = flatHealthMultiplier();
        if (flat > 0.0D) {
            playerCount = flat;
        }
        double scalar = playerCount > 1.0D ? (playerCount - 1.0D) * healthMultiplierPerPlayer() : 0.0D;
        AttributeModifier existing = maxHealth.getModifier(MODIFIER_ID);
        double currentScalar = existing == null ? 0.0D : existing.amount();
        boolean scalarChanged = Math.abs(currentScalar - scalar) >= 1.0E-6D;
        if (scalarChanged) {
            if (scalar <= 0.0D) {
                maxHealth.removeModifier(MODIFIER_ID);
            } else {
                maxHealth.addOrReplacePermanentModifier(new AttributeModifier(
                        MODIFIER_ID,
                        scalar,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
            }
        }

        float newMax = (float) maxHealth.getValue();
        float newHealth = charging ? newMax * chargeFraction(living) : newMax;
        if (scalarChanged || oldHealth + HEALTH_SLOP < newHealth) {
            living.setHealth(newHealth);
        }
        if (!charging) {
            data.putFloat(LAST_HEALTH_NBT, living.getHealth());
        }
        data.putInt(PLAYER_COUNT_NBT, (int) playerCount);
    }

    /**
     * True once current HP drops below the last filled amount. A max-HP increase
     * that left current HP on the old cap is not treated as damage.
     */
    private static boolean hasTakenDamage(CompoundTag data, float health) {
        if (!data.contains(LAST_HEALTH_NBT)) {
            return false;
        }
        return health + HEALTH_SLOP < data.getFloat(LAST_HEALTH_NBT);
    }

    private static boolean isCharging(LivingEntity living) {
        return living instanceof WitherBoss wither && wither.getInvulnerableTicks() > 0;
    }

    private static float chargeFraction(LivingEntity living) {
        if (!(living instanceof WitherBoss wither)) {
            return 1.0F;
        }
        return 1.0F - Mth.clamp(wither.getInvulnerableTicks() / 220.0F, 0.0F, 1.0F);
    }

    private static double countPlayers(LivingEntity living) {
        Level level = living.level();
        if (!useProximityScaling()) {
            return level.players().size() + VansqDebugState.extraFakePlayersIn(level);
        }
        Vec3 pos = living.position();
        double range = proximityScalingRange();
        AABB box = new AABB(
                pos.x - range, pos.y - range, pos.z - range,
                pos.x + range, pos.y + range, pos.z + range
        );
        List<Entity> nearby = level.getEntities(living, box, entity -> entity instanceof Player);
        Set<Entity> inRange = new HashSet<>();
        for (Entity entity : nearby) {
            if (pos.distanceTo(entity.position()) <= range) {
                inRange.add(entity);
            }
        }
        return inRange.size() + VansqDebugState.extraFakePlayersNear(level, pos, range);
    }

    private static boolean resolve() {
        if (resolved) {
            return isBossMethod != null;
        }
        resolved = true;
        try {
            isBossMethod = Class.forName("co.basin.multiplayerbosses.MultiplayerBosses")
                    .getMethod("isBoss", LivingEntity.class);
            Class<?> helper = Class.forName("co.basin.multiplayerbosses.platform.services.IConfigHelper");
            Object helperInstance = Class.forName("co.basin.multiplayerbosses.platform.Services")
                    .getField("CONFIG")
                    .get(null);
            shouldScale = helper.getMethod("shouldScaleBossHealth");
            useProximity = helper.getMethod("useProximityScaling");
            proximityRange = helper.getMethod("proximityScalingRange");
            perPlayer = helper.getMethod("healthMultiplierPerPlayer");
            flatHealth = helper.getMethod("flatHealthMultiplier");
            config = helperInstance;
            return true;
        } catch (ReflectiveOperationException e) {
            VansqMod.LOGGER.debug("MultiplayerBosses health rescale unavailable: {}", e.toString());
            isBossMethod = null;
            return false;
        }
    }

    private static boolean isBoss(LivingEntity living) {
        try {
            return Boolean.TRUE.equals(isBossMethod.invoke(null, living));
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private static boolean scaleHealthEnabled() {
        return invokeBoolean(shouldScale);
    }

    private static boolean useProximityScaling() {
        return invokeBoolean(useProximity);
    }

    private static int proximityScalingRange() {
        try {
            Object value = proximityRange.invoke(config);
            return value instanceof Number number ? number.intValue() : 100;
        } catch (ReflectiveOperationException e) {
            return 100;
        }
    }

    private static double healthMultiplierPerPlayer() {
        return invokeDouble(perPlayer, 0.5D);
    }

    private static double flatHealthMultiplier() {
        return invokeDouble(flatHealth, 0.0D);
    }

    private static boolean invokeBoolean(Method method) {
        try {
            return Boolean.TRUE.equals(method.invoke(config));
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private static double invokeDouble(Method method, double fallback) {
        try {
            Object value = method.invoke(config);
            return value instanceof Number number ? number.doubleValue() : fallback;
        } catch (ReflectiveOperationException e) {
            return fallback;
        }
    }
}
