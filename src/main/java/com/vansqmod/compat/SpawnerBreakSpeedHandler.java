package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Vanilla spawners and Quark monster boxes mine at default speed in Peaceful/Easy,
 * 2× slower on Normal, and 5× slower on Hard.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class SpawnerBreakSpeedHandler {

    private SpawnerBreakSpeedHandler() {
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!event.getState().is(Blocks.SPAWNER) && !MonsterBoxRework.isMonsterBox(event.getState())) {
            return;
        }
        Player player = event.getEntity();
        if (player.getAbilities().instabuild) {
            return;
        }
        // If/else instead of switch: Connector cannot load the synthetic $SwitchMap inner class.
        Difficulty difficulty = player.level().getDifficulty();
        float divisor = 1.0F;
        if (difficulty == Difficulty.NORMAL) {
            divisor = 2.0F;
        } else if (difficulty == Difficulty.HARD) {
            divisor = 5.0F;
        }
        if (divisor != 1.0F) {
            event.setNewSpeed(event.getNewSpeed() / divisor);
        }
    }
}
