package com.vansqmod.mixin.essential;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Short-circuits Essential's update flow. {@code showUpdateModal} existed through 1.4.1 and was
 * removed in 1.4.1.1, so that inject is optional.
 * Kotlin {@code Unit} is resolved via reflection so this handler never references vansqmod types
 * from Essential's classloader.
 */
@Mixin(targets = "gg.essential.util.AutoUpdate", remap = false)
public abstract class EssentialAutoUpdateMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void vansqmod$skipLoaderUpdate(CallbackInfo ci) {
        vansqmod$ignorePendingUpdate();
    }

    @Inject(method = "showUpdateModal", at = @At("HEAD"), cancellable = true, remap = false, require = 0, expect = 0)
    private void vansqmod$skipUpdateModal(
            @Coerce Object modalFlow,
            @Coerce Object continuation,
            CallbackInfoReturnable<Object> cir
    ) {
        cir.setReturnValue(vansqmod$kotlinUnit());
    }

    @Inject(method = "requiresUpdate", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$neverRequiresUpdate(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "isUpdateRequired", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$neverUpdateRequired(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "getSeenUpdateToast", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$alreadySeenUpdateToast(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Unique
    private static void vansqmod$ignorePendingUpdate() {
        try {
            Class<?> type = Class.forName("gg.essential.util.AutoUpdate");
            Object instance = type.getField("INSTANCE").get(null);
            type.getMethod("setAutoUpdates", boolean.class).invoke(instance, false);
            type.getMethod("ignoreUpdate").invoke(instance);
        } catch (Throwable ignored) {
        }
    }

    @Unique
    private static Object vansqmod$kotlinUnit() {
        try {
            return Class.forName("kotlin.Unit").getField("INSTANCE").get(null);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
