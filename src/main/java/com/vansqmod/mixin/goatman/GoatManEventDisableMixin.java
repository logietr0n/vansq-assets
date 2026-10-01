package com.vansqmod.mixin.goatman;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

public final class GoatManEventDisableMixin {

    private GoatManEventDisableMixin() {
    }

    @Mixin(targets = "de.cadentem.goat_man.GoatMan", remap = false)
    public abstract static class Omen {

        @Inject(method = "handleOmenGoats", at = @At("HEAD"), cancellable = true)
        private void vansqmod$disableOmenGoats(ServerLevel level, List<?> players, CallbackInfo ci) {
            ci.cancel();
        }
    }

    @Mixin(targets = "de.cadentem.goat_man.util.GoatEventManager", remap = false)
    public abstract static class Followed {

        @Inject(method = "tryStartCharger", at = @At("HEAD"), cancellable = true)
        private static void vansqmod$disableFollowed(CallbackInfo ci) {
            ci.cancel();
        }

        @Redirect(
                method = "resolveHit",
                at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/server/level/ServerPlayer;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z",
                        remap = true
                ),
                require = 0
        )
        private static boolean vansqmod$skipHitDarkness(ServerPlayer player, MobEffectInstance effect) {
            return false;
        }
    }
}
