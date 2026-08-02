package com.vansqmod.mixin.effortless;

import dev.huskuraft.effortless.api.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses Effortless status spam in chat while keeping action-bar build feedback.
 */
@Mixin(targets = "dev.huskuraft.effortless.neoforge.core.MinecraftPlayer", remap = false)
public abstract class EffortlessSuppressChatMixin {

    @Inject(
            method = "sendMessage(Ldev/huskuraft/effortless/api/text/Text;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void vansqmod$suppressChat(Text message, boolean actionBar, CallbackInfo ci) {
        if (!actionBar) {
            ci.cancel();
        }
    }
}
