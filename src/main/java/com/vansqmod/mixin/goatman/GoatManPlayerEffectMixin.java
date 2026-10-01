package com.vansqmod.mixin.goatman;

import com.vansqmod.compat.GoatManPlayerEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Drop every potion effect Goat Man tries to apply, regardless of invoke owner.
 */
@Mixin(LivingEntity.class)
public abstract class GoatManPlayerEffectMixin {

    @Inject(
            method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void vansqmod$blockGoatManEffects(
            MobEffectInstance effect,
            Entity source,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (GoatManPlayerEffects.isGoatManCaller()) {
            cir.setReturnValue(false);
        }
    }
}
