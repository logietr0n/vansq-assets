package com.vansqmod.mixin.multiplayerbosses;

import com.vansqmod.debug.VansqDebugState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Set;

/**
 * Makes {@code /vansq debug playercount} affect MultiplayerBosses spawn scaling
 * without spawning real player entities.
 */
@Mixin(targets = "co.basin.multiplayerbosses.events.Events", remap = false)
public abstract class MultiplayerBossesPlayerCountMixin {

    @Redirect(
            method = "onMobSpawn",
            at = @At(value = "INVOKE", target = "Ljava/util/List;size()I")
    )
    private static int vansqmod$globalFakePlayers(List<?> list, LivingEntity living, Level level) {
        return list.size() + VansqDebugState.extraFakePlayersIn(level);
    }

    @Redirect(
            method = "onMobSpawn",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;size()I")
    )
    private static int vansqmod$nearbyFakePlayers(Set<?> set, LivingEntity living, Level level) {
        return set.size() + VansqDebugState.extraFakePlayersAmong(set);
    }
}
