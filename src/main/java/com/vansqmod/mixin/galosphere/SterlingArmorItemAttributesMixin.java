package com.vansqmod.mixin.galosphere;

import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Armor/toughness live in {@link com.vansqmod.compat.PackItemAttributes}. This only zeros Galosphere's Illager Resistance method.
 */
@Mixin(targets = "net.orcinus.galosphere.items.SterlingArmorItem", remap = false)
public abstract class SterlingArmorItemAttributesMixin {

    @Inject(
            method = "getIllagerResistance(Lnet/minecraft/world/entity/EquipmentSlot;)F",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void vansqmod$noIllagerResistance(EquipmentSlot slot, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(0.0F);
    }
}
