package com.vansqmod.mixin.artistry;

import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skip attaching {@code RunAwayFromBlockGoal} on spawn. That method only adds the
 * corpse-flower flee goal.
 */
@Mixin(targets = "com.feliscape.artistry.content.event.Events", remap = false)
public abstract class ArtistryCorpseFlowerScareMixin {

    @Inject(method = "onEntitySpawn", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$skipCorpseFlowerScare(EntityJoinLevelEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}
