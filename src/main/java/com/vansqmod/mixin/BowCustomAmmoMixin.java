package com.vansqmod.mixin;

import com.vansqmod.compat.RangedAmmo;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(BowItem.class)
public abstract class BowCustomAmmoMixin {

    @Inject(method = "getAllSupportedProjectiles", at = @At("RETURN"), cancellable = true)
    private void vansqmod$appendCustomAmmo(CallbackInfoReturnable<Predicate<ItemStack>> cir) {
        Predicate<ItemStack> base = cir.getReturnValue();
        if (base != null) {
            cir.setReturnValue(base.or(RangedAmmo::isAmmo));
        }
    }
}
