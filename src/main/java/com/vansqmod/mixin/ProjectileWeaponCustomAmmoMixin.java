package com.vansqmod.mixin;

import com.vansqmod.compat.RangedAmmo;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProjectileWeaponItem.class)
public abstract class ProjectileWeaponCustomAmmoMixin {

    @Inject(method = "createProjectile", at = @At("HEAD"), cancellable = true)
    private void vansqmod$createCustomAmmo(
            Level level,
            LivingEntity shooter,
            ItemStack weapon,
            ItemStack ammo,
            boolean crit,
            CallbackInfoReturnable<Projectile> cir
    ) {
        Projectile projectile = RangedAmmo.create(level, shooter, weapon, ammo);
        if (projectile != null) {
            RangedAmmo.applyCrit(projectile, ammo, crit);
            cir.setReturnValue(projectile);
        }
    }
}
