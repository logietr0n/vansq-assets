package com.vansqmod.mixin;

import com.vansqmod.entity.BabyZombieHealth;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Re-applies half max health whenever a zombie-family mob is marked baby or grows up.
 */
@Mixin(Zombie.class)
public abstract class BabyZombieHealthMixin {

    @Inject(method = "setBaby", at = @At("RETURN"))
    private void vansqmod$babyHalfHealth(boolean baby, CallbackInfo ci) {
        BabyZombieHealth.apply((Zombie) (Object) this);
    }
}
