package com.vansqmod.mixin.refraction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Refraction starts an ImGui frame in {@code Minecraft.runTick}. Quick Play and
 * {@code Minecraft.disconnect} nest another {@code runTick} while the loading overlay
 * is still rendering, so {@code beginFrame} hits "Forgot to call Render() or EndFrame()"
 * and native imgui aborts with {@code 0xc0000409}. Skip nested begin/end pairs.
 */
@Mixin(targets = "net.refractionapi.refraction.gui.RIMGuiInternal", remap = false)
public class RIMGuiInternalReentryMixin {

    @Unique
    private int vansqmod$imguiDepth;

    @Inject(method = "beginFrame", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$skipNestedBegin(CallbackInfo ci) {
        if (this.vansqmod$imguiDepth++ > 0) {
            ci.cancel();
        }
    }

    @Inject(method = "endFrame", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$skipNestedEnd(CallbackInfo ci) {
        if (this.vansqmod$imguiDepth > 0) {
            this.vansqmod$imguiDepth--;
        }
        if (this.vansqmod$imguiDepth > 0) {
            ci.cancel();
        }
    }
}
