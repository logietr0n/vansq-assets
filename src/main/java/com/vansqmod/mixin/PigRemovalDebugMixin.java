package com.vansqmod.mixin;

import com.vansqmod.entity.PigForcePersist;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Only real deaths/discards are intentional. Chunk unload / dimension change must not
 * block force-persist recovery — that was wiping pigs on rejoin.
 */
@Mixin(Entity.class)
public abstract class PigRemovalDebugMixin {

    @Inject(method = "remove", at = @At("HEAD"))
    private void vansqmod$pigIntentionalRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self instanceof Pig) {
            PigForcePersist.markIntentionalRemoval(self, reason);
        }
    }

    @Inject(method = "setRemoved", at = @At("HEAD"))
    private void vansqmod$pigIntentionalSetRemoved(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self instanceof Pig) {
            PigForcePersist.markIntentionalRemoval(self, reason);
        }
    }

    @Inject(method = "discard", at = @At("HEAD"))
    private void vansqmod$pigIntentionalDiscard(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self instanceof Pig) {
            PigForcePersist.markIntentionalRemoval(self, Entity.RemovalReason.DISCARDED);
        }
    }
}
