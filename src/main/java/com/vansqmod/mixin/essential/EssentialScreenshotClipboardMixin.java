package com.vansqmod.mixin.essential;

import com.vansqmod.compat.EssentialScreenshotClipboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

/**
 * Essential copies screenshots by forking a JVM whose classpath includes nested Jar-in-Jar URLs
 * that are not real files (KotlinLangForge {@code kotlin-stdlib}). That throws and the clipboard
 * never updates. Copy in-process instead.
 */
@Mixin(targets = "gg.essential.gui.screenshot.UtilsKt", remap = false)
public abstract class EssentialScreenshotClipboardMixin {

    @Inject(
            method = "copyScreenshotToClipboard(Ljava/nio/file/Path;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void vansqmod$copyInProcess(Path screenshot, CallbackInfo ci) {
        if (EssentialScreenshotClipboard.copyPng(screenshot)) {
            EssentialScreenshotClipboard.notifyCopied();
        } else {
            EssentialScreenshotClipboard.notifyFailed();
        }
        ci.cancel();
    }
}
