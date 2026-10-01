package com.vansqmod.mixin.enchantwithmob;

import com.vansqmod.compat.EnchantedSpawnRates;
import com.vansqmod.debug.VansqDebugState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces EnchantWithMob's toml chance roll with {@link EnchantedSpawnRates}.
 * {@code /vansq debug chaos} multiplies that chance by 4 (capped at 100%).
 * {@code /vansq debug mobenchant} still forces the first {@code nextFloat()} to succeed.
 * Later rolls (enchantment budget, trial-spawner Wind) stay real random.
 */
@Mixin(targets = "baguchi.enchantwithmob.CommonEventHandler", remap = false)
public abstract class EnchantedSpawnChanceMixin {

    private static final ThreadLocal<FinalizeSpawnEvent> CURRENT = new ThreadLocal<>();

    @Inject(method = "onSpawnEntity", at = @At("HEAD"))
    private static void vansqmod$captureEnchantSpawn(FinalizeSpawnEvent event, CallbackInfo ci) {
        CURRENT.set(event);
    }

    @Inject(method = "onSpawnEntity", at = @At("RETURN"))
    private static void vansqmod$clearEnchantSpawn(FinalizeSpawnEvent event, CallbackInfo ci) {
        CURRENT.remove();
    }

    @Redirect(
            method = "onSpawnEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/RandomSource;nextFloat()F",
                    ordinal = 0,
                    remap = true
            ),
            remap = false
    )
    private static float vansqmod$enchantSpawnChance(RandomSource random) {
        if (VansqDebugState.isForceMobEnchantEnabled()) {
            return -1.0F;
        }
        FinalizeSpawnEvent event = CURRENT.get();
        float chance = 0.0F;
        if (event != null && event.getLevel() instanceof ServerLevel server) {
            chance = EnchantedSpawnRates.chance(server, event.getEntity().blockPosition());
        }
        chance = Math.min(1.0F, chance * VansqDebugState.chaosEnchantMultiplier());
        return random.nextFloat() < chance ? 0.0F : 1.0F;
    }
}
