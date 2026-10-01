package com.vansqmod.mixin.goatman;

import com.vansqmod.compat.GoatManDifficulty;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Peaceful/Easy: no Goat Man presence (spawns, omen goats, cave noises, events).
 * Hardcore: flee after losing 10 health.
 */
public final class GoatManDifficultyMixin {

    private GoatManDifficultyMixin() {
    }

    @Mixin(targets = "de.cadentem.goat_man.GoatMan", remap = false)
    public abstract static class Tick {

        @Inject(method = "handleLogic", at = @At("HEAD"), cancellable = true)
        private void vansqmod$suppressEasy(ServerLevel level, CallbackInfo ci) {
            if (!GoatManDifficulty.isSuppressed(level)) {
                return;
            }
            GoatManDifficulty.discardAll(level);
            ci.cancel();
        }
    }

    @Mixin(targets = "de.cadentem.goat_man.entities.GoatManEntity")
    public abstract static class EntityTick {

        @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
        private void vansqmod$suppressOrFlee(CallbackInfo ci) {
            Entity self = (Entity) (Object) this;
            Level level = self.level();
            if (level.isClientSide) {
                return;
            }
            if (GoatManDifficulty.isSuppressed(level)) {
                self.discard();
                ci.cancel();
            }
        }

        @Inject(method = "tick", at = @At("RETURN"))
        private void vansqmod$hardcoreFlee(CallbackInfo ci) {
            LivingEntity self = (LivingEntity) (Object) this;
            if (!self.level().isClientSide) {
                GoatManDifficulty.tryHardcoreFlee(self);
            }
        }
    }
}
