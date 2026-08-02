package com.vansqmod.compat;

import com.vansqmod.item.RhodoheartShardItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

/**
 * Heartstone pairing: heal the connected player for 6 HP with Rhodoheart FX.
 * Stack size stays at Heartstone's default (no durability override).
 */
public final class HeartstoneCompat {

    private static final float HEAL_AMOUNT = 6.0F;

    private HeartstoneCompat() {
    }

    /**
     * Called when Heartstone successfully reveals the bound player (either user).
     */
    public static void onSuccessfulReveal(Player user, Player bound) {
        if (user == null || bound == null || user.level().isClientSide()) {
            return;
        }
        if (!(user.level() instanceof ServerLevel level)) {
            return;
        }

        bound.heal(HEAL_AMOUNT);
        RhodoheartShardItem.playHealEffects(level, bound);
    }
}
