package com.vansqmod.boss;

import com.vansqmod.VansqMod;
import com.vansqmod.registry.ModAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

/**
 * Shared fight-active checks for vansq boss bars and client music.
 */
public final class BossFightState {

    private static final String BERSERKER_CLASS = "net.orcinus.galosphere.entities.Berserker";

    private static Method berserkerIsStationary;
    private static Method berserkerIsShedding;
    private static boolean berserkerResolved;

    private BossFightState() {
    }

    public static void tickServer(LivingEntity living) {
        if (living.level().isClientSide()) {
            return;
        }
        ResourceLocation id = typeId(living);
        if (!BossBarRegistry.MISSIONER.equals(id) && !BossBarRegistry.MUTANT_ENDERMAN.equals(id)) {
            return;
        }
        if (Boolean.TRUE.equals(living.getData(ModAttachments.BOSS_FIGHT_ACTIVE.get()))) {
            return;
        }
        if (!(living instanceof Mob mob)) {
            return;
        }
        if (mob.getTarget() instanceof Player player && player.isAlive() && !player.isSpectator()) {
            living.setData(ModAttachments.BOSS_FIGHT_ACTIVE.get(), Boolean.TRUE);
        }
    }

    public static boolean isFightActive(LivingEntity living) {
        ResourceLocation id = typeId(living);
        if (BossBarRegistry.BERSERKER.equals(id)) {
            return isBerserkerEngaged(living);
        }
        if (BossBarRegistry.MISSIONER.equals(id) || BossBarRegistry.MUTANT_ENDERMAN.equals(id)) {
            return Boolean.TRUE.equals(living.getData(ModAttachments.BOSS_FIGHT_ACTIVE.get()));
        }
        return true;
    }

    private static ResourceLocation typeId(LivingEntity living) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(living.getType());
    }

    /**
     * Statue/frozen form stays quiet. Bar and music start when the statue is
     * first engaged (shedding wake-up) and stay once it is fully awake.
     */
    private static boolean isBerserkerEngaged(LivingEntity living) {
        resolveBerserker();
        if (berserkerIsStationary == null) {
            return true;
        }
        try {
            boolean stationary = (Boolean) berserkerIsStationary.invoke(living);
            if (!stationary) {
                return true;
            }
            return berserkerIsShedding != null && (Boolean) berserkerIsShedding.invoke(living);
        } catch (ReflectiveOperationException | ClassCastException e) {
            VansqMod.LOGGER.debug("Could not read Berserker statue state: {}", e.toString());
            return true;
        }
    }

    private static void resolveBerserker() {
        if (berserkerResolved) {
            return;
        }
        berserkerResolved = true;
        if (!ModList.get().isLoaded("galosphere")) {
            return;
        }
        try {
            Class<?> type = Class.forName(BERSERKER_CLASS);
            berserkerIsStationary = type.getMethod("isStationary");
            berserkerIsShedding = type.getMethod("isShedding");
        } catch (ReflectiveOperationException e) {
            VansqMod.LOGGER.debug("Berserker class unavailable: {}", e.toString());
        }
    }
}
