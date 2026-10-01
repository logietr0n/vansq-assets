package com.vansqmod.compat;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public final class GoatManPlayerEffects {

    private static final String GOAT_MAN_PACKAGE = "de.cadentem.goat_man";

    private GoatManPlayerEffects() {
    }

    public static boolean addEffect(LivingEntity entity, MobEffectInstance effect) {
        return false;
    }

    public static boolean isGoatManCaller() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        int limit = Math.min(stack.length, 18);
        for (int i = 2; i < limit; i++) {
            if (stack[i].getClassName().startsWith(GOAT_MAN_PACKAGE)) {
                return true;
            }
        }
        return false;
    }
}
