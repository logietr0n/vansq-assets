package com.vansqmod.mixin.variantsandventures;

import com.faboslav.variantsandventures.common.init.VariantsAndVenturesSoundEvents;
import com.vansqmod.compat.GelidSnowballs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Snowball.class)
public abstract class SnowballGelidHitMixin {

    @Inject(method = "onHitEntity", at = @At("RETURN"))
    private void vansqmod$gelidSnowballHit(EntityHitResult result, CallbackInfo ci) {
        Snowball snowball = (Snowball) (Object) this;
        if (!GelidSnowballs.isGelid(snowball.getOwner())) {
            return;
        }
        Entity target = result.getEntity();
        if (!(target instanceof LivingEntity living)) {
            return;
        }
        living.playSound(
                VariantsAndVenturesSoundEvents.ENTITY_SNOWBALL_IMPACT.get(),
                1.0F,
                0.4F / (living.getRandom().nextFloat() * 0.4F + 0.8F));
        target.hurt(
                snowball.damageSources().thrown(snowball, snowball.getOwner()),
                GelidSnowballs.SNOWBALL_GELID_DAMAGE);
        GelidSnowballs.setFreeze(target, GelidSnowballs.SNOWBALL_GELID_FREEZE_TICKS);
    }
}
