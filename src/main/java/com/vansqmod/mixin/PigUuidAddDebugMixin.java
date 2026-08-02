package com.vansqmod.mixin;

import com.vansqmod.VansqMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PersistentEntitySectionManager.class)
public abstract class PigUuidAddDebugMixin {

    @Inject(method = "addEntityUuid", at = @At("HEAD"))
    private void vansqmod$logPigUuidCollision(EntityAccess entity, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof Pig pig)) {
            return;
        }
        PersistentEntitySectionManagerAccessor accessor = (PersistentEntitySectionManagerAccessor) this;
        if (accessor.vansqmod$getKnownUuids().contains(pig.getUUID())) {
            VansqMod.LOGGER.error(
                    "[PigForcePersist] UUID already in knownUuids before add: uuid={} isAddedToLevel={} removed={}",
                    pig.getUUID(),
                    pig.isAddedToLevel(),
                    pig.isRemoved(),
                    new RuntimeException("Pig UUID collision"));
        }
    }
}
