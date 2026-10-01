package com.vansqmod.mixin.variantsandventures;

import com.vansqmod.compat.GelidSnowballs;
import com.vansqmod.entity.IceSnowballProjectile;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.faboslav.variantsandventures.common.entity.mob.GelidEntity", remap = false)
public abstract class GelidThrowSnowballMixin {

    @Inject(method = "throwSnowball", at = @At("HEAD"), cancellable = true)
    private void vansqmod$throwIceSnowball(LivingEntity target, float pullProgress, CallbackInfo ci) {
        Mob gelid = (Mob) (Object) this;
        ItemStack offhand = gelid.getItemInHand(InteractionHand.OFF_HAND);
        if (!GelidSnowballs.isIceSnowball(offhand)) {
            return;
        }
        ItemStack thrown = offhand.copyWithCount(1);
        offhand.shrink(1);
        IceSnowballProjectile snowball = new IceSnowballProjectile(gelid.level(), gelid, thrown);
        double aimY = target.getEyeY() - 1.1F;
        double dx = target.getX() - gelid.getX();
        double dy = aimY - snowball.getY();
        double dz = target.getZ() - gelid.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz) * 0.2D;
        snowball.shoot(dx, dy + horizontal, dz, 1.6F, 7.0F);
        gelid.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (gelid.getRandom().nextFloat() * 0.4F + 0.8F));
        gelid.swing(InteractionHand.OFF_HAND);
        gelid.level().addFreshEntity(snowball);
        ci.cancel();
    }
}
