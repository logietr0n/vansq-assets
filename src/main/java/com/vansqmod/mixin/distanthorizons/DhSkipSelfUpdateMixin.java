package com.vansqmod.mixin.distanthorizons;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Distant Horizons 3.3.x's self-updater static-inits {@code VERSION_CONSTANTS} before
 * DH's injector has bound it, which NPEs during the loading overlay and takes down
 * the client. Skip that prompt; pack updates go through Modrinth.
 * <p>
 * Do not run {@code continueLoading} here. DH's {@code MixinMinecraft.buildInitialScreens}
 * already invokes the original runnable after this method returns.
 */
@Mixin(
        targets = "com.seibel.distanthorizons.common.commonMixins.DhUpdateScreenBase_neoforge",
        remap = false
)
public abstract class DhSkipSelfUpdateMixin {

    @Inject(method = "tryShowUpdateScreenAndRunAutoUpdateStartup", at = @At("HEAD"), cancellable = true, remap = false)
    private static void vansqmod$skipBrokenSelfUpdate(Runnable continueLoading, CallbackInfo ci) {
        ci.cancel();
    }
}
