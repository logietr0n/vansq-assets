package com.vansqmod.mixin.artistry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Artistry attaches this goal to almost every PathfinderMob so they flee corpse flowers.
 * Those flowers do not generate in this pack, but the goal still scans a 5\times3 volume
 * every tick. Disable it; existing mobs keep the no-op until they despawn.
 */
@Mixin(targets = "com.feliscape.artistry.content.entity.ai.RunAwayFromBlockGoal", remap = false)
public abstract class RunAwayFromBlockGoalMixin {

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void vansqmod$disableCorpseFlowerFlee(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
