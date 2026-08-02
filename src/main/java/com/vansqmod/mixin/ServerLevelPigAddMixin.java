package com.vansqmod.mixin;

import com.vansqmod.entity.PigForcePersist;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelPigAddMixin {

    @Inject(method = "addEntity", at = @At("RETURN"))
    private void vansqmod$pigForcePersist(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof Pig pig)) {
            return;
        }
        Boolean returned = cir.getReturnValue();
        PigForcePersist.onAddFinished((ServerLevel) (Object) this, pig, returned != null && returned);
    }
}
