package com.vansqmod.mixin.goatman;

import com.vansqmod.client.GoatManMusicSilence;
import com.vansqmod.compat.GoatManDifficulty;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Block Goat Man stalk/chase tracks. Cave noises stay off on Peaceful/Easy.
 */
public final class GoatManClientDifficultyMixin {

    private GoatManClientDifficultyMixin() {
    }

    @Mixin(targets = "de.cadentem.goat_man.client.HandleMusic", remap = false)
    public abstract static class Music {

        @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
        private static void vansqmod$suppressPackets(@Coerce Object packet, CallbackInfo ci) {
            GoatManMusicSilence.onMusicPacket(packet);
            ci.cancel();
        }
    }

    @Mixin(targets = "de.cadentem.goat_man.client.HandleCaveSound", remap = false)
    public abstract static class CaveSound {

        @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
        private static void vansqmod$suppress(@Coerce Object packet, CallbackInfo ci) {
            Level level = Minecraft.getInstance().level;
            if (GoatManDifficulty.isSuppressed(level)) {
                ci.cancel();
            }
        }
    }
}
