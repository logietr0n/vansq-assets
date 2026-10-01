package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.lang.reflect.Field;

/**
 * Missionary uses Born in Chaos's {@code forge:spirit} tag. Restless Spirit is the only vanilla-BiC
 * mob that actually runs the sun-despawn procedure, so we apply that same daylight vanish here.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class MissionerSpiritDespawnHandler {

    private static final ResourceLocation MISSIONER =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "missioner");
    private static final ResourceLocation DARK_SMOKE =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "dark_smoke");

    private static GameRules.Key<GameRules.BooleanValue> disappearKey;
    private static boolean disappearKeyResolved;

    private MissionerSpiritDespawnHandler() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide() || !isMissioner(entity)) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        if (!shouldVanishInSun(level, entity)) {
            return;
        }
        spawnSmoke(level, entity);
        entity.discard();
    }

    private static boolean shouldVanishInSun(ServerLevel level, Entity entity) {
        if (!disappearInSunEnabled(level)) {
            return false;
        }
        if (!level.isDay() || level.isRaining() || level.isThundering()) {
            return false;
        }
        BlockPos skyCheck = BlockPos.containing(entity.getX(), entity.getY() + 1.0D, entity.getZ());
        return level.canSeeSkyFromBelowWater(skyCheck);
    }

    private static boolean disappearInSunEnabled(Level level) {
        GameRules.Key<GameRules.BooleanValue> key = spiritSunGamerule();
        if (key == null) {
            return true;
        }
        return level.getGameRules().getBoolean(key);
    }

    @SuppressWarnings("unchecked")
    private static GameRules.Key<GameRules.BooleanValue> spiritSunGamerule() {
        if (disappearKeyResolved) {
            return disappearKey;
        }
        disappearKeyResolved = true;
        try {
            Class<?> rules = Class.forName("net.mcreator.borninchaosv.init.BornInChaosV1ModGameRules");
            Field field = rules.getField("DISAPPEARANCEOFSPIRITSUNDERTHESUN");
            disappearKey = (GameRules.Key<GameRules.BooleanValue>) field.get(null);
        } catch (ReflectiveOperationException ignored) {
            disappearKey = null;
        }
        return disappearKey;
    }

    private static void spawnSmoke(ServerLevel level, Entity entity) {
        ParticleOptions particle = BuiltInRegistries.PARTICLE_TYPE.getOptional(DARK_SMOKE)
                .filter(SimpleParticleType.class::isInstance)
                .map(SimpleParticleType.class::cast)
                .orElse(ParticleTypes.LARGE_SMOKE);
        level.sendParticles(particle, entity.getX(), entity.getY(), entity.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.1D);
    }

    private static boolean isMissioner(Entity entity) {
        return MISSIONER.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }
}
