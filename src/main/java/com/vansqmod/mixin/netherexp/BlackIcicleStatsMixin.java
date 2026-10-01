package com.vansqmod.mixin.netherexp;

import com.vansqmod.compat.RangedAmmo;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.jadenxgamer.netherexp.core.entity.BlackIcicle")
public abstract class BlackIcicleStatsMixin {

    private static final double BASE_DAMAGE = RangedAmmo.BLACK_ICICLE_BASE_DAMAGE;
    private static final int FREEZE_TICKS = 120;

    @Inject(
            method = {
                    "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V",
                    "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V",
                    "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V"
            },
            at = @At("RETURN")
    )
    private void vansqmod$setDamage(CallbackInfo ci) {
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        arrow.setBaseDamage(BASE_DAMAGE);
        arrow.setCritArrow(false);
    }

    @Redirect(
            method = "onHitEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/common/ModConfigSpec$IntValue;get()Ljava/lang/Object;"
            )
    )
    private Object vansqmod$freezeTicks(ModConfigSpec.IntValue value) {
        return FREEZE_TICKS;
    }
}
