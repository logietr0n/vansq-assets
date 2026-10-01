package com.vansqmod.mixin.essential;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Blocks the Kotlin entry points that open available/required update modals.
 * Kotlin {@code Unit} is resolved via reflection so this handler never references vansqmod types
 * from Essential's classloader.
 */
@Mixin(targets = "gg.essential.gui.modals.UpdateAvailableModalKt", remap = false)
public abstract class EssentialUpdateAvailableModalKtMixin {

    @Inject(method = "updateAvailableModal", at = @At("HEAD"), cancellable = true, remap = false)
    private static void vansqmod$skipAvailable(
            @Coerce Object modalFlow,
            @Coerce Object continuation,
            CallbackInfoReturnable<Object> cir
    ) {
        cir.setReturnValue(vansqmod$kotlinUnit());
    }

    @Inject(method = "updateRequiredModal", at = @At("HEAD"), cancellable = true, remap = false)
    private static void vansqmod$skipRequired(
            @Coerce Object modalFlow,
            @Coerce Object continuation,
            CallbackInfoReturnable<Object> cir
    ) {
        cir.setReturnValue(vansqmod$kotlinUnit());
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
