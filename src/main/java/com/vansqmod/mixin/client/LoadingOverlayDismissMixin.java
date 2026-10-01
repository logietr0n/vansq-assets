package com.vansqmod.mixin.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * FancyMenu can keep {@link LoadingOverlay} on screen after the client resource
 * reload is already done. Vanilla's fade then never reaches {@code setOverlay(null)},
 * so the title/world sits under a frozen load sequence. Drop it after a short grace.
 */
@Mixin(Minecraft.class)
public abstract class LoadingOverlayDismissMixin {

    @Shadow
    public abstract Overlay getOverlay();

    @Shadow
    public abstract void setOverlay(Overlay overlay);

    @Unique
    private long vansqmod$reloadDoneAt = -1L;

    @Inject(method = "runTick", at = @At("TAIL"))
    private void vansqmod$dismissStuckOverlay(boolean renderLevel, CallbackInfo ci) {
        Overlay overlay = this.getOverlay();
        if (!(overlay instanceof LoadingOverlay loading)) {
            this.vansqmod$reloadDoneAt = -1L;
            return;
        }
        ReloadInstance reload = ((LoadingOverlayReloadAccessor) loading).vansqmod$reload();
        if (!reload.isDone()) {
            this.vansqmod$reloadDoneAt = -1L;
            return;
        }
        long now = Util.getMillis();
        if (this.vansqmod$reloadDoneAt < 0L) {
            this.vansqmod$reloadDoneAt = now;
            return;
        }
        if (now - this.vansqmod$reloadDoneAt >= 2000L) {
            this.setOverlay(null);
        }
    }
}
