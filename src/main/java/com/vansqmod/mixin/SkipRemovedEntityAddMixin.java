package com.vansqmod.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Several spawn-replace mods (Hominid among them) call {@code addFreshEntity} on an
 * entity that is already discarded. Vanilla then logs and returns; skipping first
 * avoids the warn spam on the server thread.
 */
@Mixin(ServerLevel.class)
public abstract class SkipRemovedEntityAddMixin {

    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void vansqmod$skipRemoved(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity != null && entity.isRemoved()) {
            cir.setReturnValue(false);
        }
    }
}
