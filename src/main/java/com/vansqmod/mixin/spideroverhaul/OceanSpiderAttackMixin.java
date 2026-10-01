package com.vansqmod.mixin.spideroverhaul;

import com.vansqmod.compat.OceanSpiderLeap;
import dev.chybx.spideroverhaul.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.chybx.spideroverhaul.entity.OceanSpiderEntity", remap = false)
public abstract class OceanSpiderAttackMixin {

    @Inject(method = "onAttackSuccess", at = @At("TAIL"))
    private void vansqmod$applyBog(Entity target, CallbackInfo ci) {
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(ModEffects.BOG, OceanSpiderLeap.BOG_DURATION_TICKS, 0), (Entity) (Object) this);
        }
    }
}
