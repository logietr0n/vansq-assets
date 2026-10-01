package com.vansqmod.mixin.yungscavebiomes;

import com.vansqmod.compat.FrostedCavesGelidConversion;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla zombie shake is drowned conversion only. Play the same shake while a
 * Frosted Caves zombie is turning into a Gelid.
 */
@Mixin(AbstractZombieRenderer.class)
public abstract class ZombieGelidConversionShakeMixin {

    @Inject(method = "isShaking(Lnet/minecraft/world/entity/monster/Zombie;)Z", at = @At("HEAD"), cancellable = true)
    private void vansqmod$shakeGelidConversion(Zombie zombie, CallbackInfoReturnable<Boolean> cir) {
        if (zombie instanceof FrostedCavesGelidConversion.Access access && access.vansqmod$isGelidConverting()) {
            cir.setReturnValue(true);
        }
    }
}
