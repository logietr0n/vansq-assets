package com.vansqmod.mixin;

import com.vansqmod.debug.VansqDebugState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public abstract class ZombieRareBabyOddsMixin {

    @Inject(method = "getSpawnAsBabyOdds", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$forceBabyOdds(RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        if (VansqDebugState.isForceRareEventsEnabled()) {
            cir.setReturnValue(true);
        }
    }
}
