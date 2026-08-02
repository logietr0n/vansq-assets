package com.vansqmod.mixin.essential;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keep Essential's zoom {@link KeyMapping} out of Controls and unbound, matching the
 * disabled zoom behavior in {@link EssentialZoomHandlerMixin}.
 */
@Mixin(targets = "gg.essential.key.EssentialKeybinding", remap = false)
public abstract class EssentialKeybindingHideZoomMixin {

    @Shadow
    @Final
    public KeyMapping keyBinding;

    @Shadow
    @Final
    private String keyId;

    @Inject(method = "<init>(Ljava/lang/String;Ljava/lang/String;IZ)V", at = @At("RETURN"), remap = false)
    private void vansqmod$unbindZoom(String keyId, String category, int keyCode, boolean alwaysTick, CallbackInfo ci) {
        if (!vansqmod$isZoom()) {
            return;
        }
        this.keyBinding.setKey(InputConstants.UNKNOWN);
        KeyMapping.resetMapping();
    }

    @Inject(
            method = "register([Lnet/minecraft/client/KeyMapping;)[Lnet/minecraft/client/KeyMapping;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void vansqmod$skipZoomBatchRegister(KeyMapping[] mappings, CallbackInfoReturnable<KeyMapping[]> cir) {
        if (vansqmod$isZoom()) {
            cir.setReturnValue(mappings);
        }
    }

    @Inject(method = "register()V", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$skipZoomRegister(CallbackInfo ci) {
        if (vansqmod$isZoom()) {
            ci.cancel();
        }
    }

    @Unique
    private boolean vansqmod$isZoom() {
        return "ZOOM".equals(this.keyId);
    }
}
