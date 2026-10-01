package com.vansqmod.compat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.lang.reflect.Field;

public final class GoatManDifficulty {

    private static final String ENTITY_CLASS = "de.cadentem.goat_man.entities.GoatManEntity";
    private static final String ROLL_CLASS = "de.cadentem.goat_man.entities.goals.Roll";
    private static final float HARDCORE_FLEE_DAMAGE = 10.0F;

    private static Field currentRoll;
    private static Object fleeRoll;
    private static boolean rollResolved;

    private GoatManDifficulty() {
    }

    public static boolean isGoatMan(Entity entity) {
        return entity != null && ENTITY_CLASS.equals(entity.getClass().getName());
    }

    public static boolean isSuppressed(Level level) {
        if (level == null) {
            return false;
        }
        Difficulty difficulty = level.getDifficulty();
        return difficulty == Difficulty.PEACEFUL || difficulty == Difficulty.EASY;
    }

    public static boolean isHardcoreLenient(Level level) {
        return level != null && level.getLevelData().isHardcore();
    }

    public static void discardAll(ServerLevel level) {
        for (Entity entity : level.getAllEntities()) {
            if (isGoatMan(entity)) {
                entity.discard();
            }
        }
    }

    public static void tryHardcoreFlee(LivingEntity goatMan) {
        if (!isHardcoreLenient(goatMan.level())) {
            return;
        }
        if (goatMan.getMaxHealth() - goatMan.getHealth() < HARDCORE_FLEE_DAMAGE) {
            return;
        }
        ensureRoll();
        if (currentRoll == null || fleeRoll == null) {
            return;
        }
        try {
            currentRoll.set(goatMan, fleeRoll);
        } catch (IllegalAccessException ignored) {
            return;
        }
        if (goatMan instanceof Mob mob && mob.getTarget() == null && mob.getLastHurtByMob() != null) {
            mob.setTarget(mob.getLastHurtByMob());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void ensureRoll() {
        if (rollResolved) {
            return;
        }
        rollResolved = true;
        try {
            Class<?> entity = Class.forName(ENTITY_CLASS);
            currentRoll = entity.getField("currentRoll");
            Class<? extends Enum> roll = (Class<? extends Enum>) Class.forName(ROLL_CLASS);
            fleeRoll = Enum.valueOf(roll, "FLEE");
        } catch (ReflectiveOperationException ignored) {
            currentRoll = null;
            fleeRoll = null;
        }
    }
}
