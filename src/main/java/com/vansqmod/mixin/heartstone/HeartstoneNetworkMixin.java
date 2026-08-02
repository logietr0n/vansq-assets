package com.vansqmod.mixin.heartstone;

import com.vansqmod.compat.HeartstoneCompat;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When Heartstone reveals a bound player, also heal both players (Rhodoheart FX + durability).
 */
@Mixin(targets = "net.mehvahdjukaar.heartstone.NetworkHandler", remap = false)
public class HeartstoneNetworkMixin {

    @Inject(method = "sendHeartstoneParticles", at = @At("HEAD"))
    private static void vansqmod$healOnReveal(Player player, Player other, CallbackInfo ci) {
        HeartstoneCompat.onSuccessfulReveal(player, other);
    }
}
