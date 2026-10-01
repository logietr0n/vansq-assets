package com.vansqmod.mixin.client;

import com.vansqmod.compat.DoubucklerHands;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * While a Doubuckler is in the main hand the real offhand item is only covered,
 * not equipped. Client code (lights, arm pose, first-person) should see the
 * Doubuckler copy instead of a torch or lantern.
 */
@Mixin(Player.class)
public abstract class DoubucklerClientOffhandMixin {

    @Inject(method = "getItemBySlot", at = @At("HEAD"), cancellable = true)
    private void vansqmod$coveredOffhand(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        if (slot != EquipmentSlot.OFFHAND) {
            return;
        }
        Player player = (Player) (Object) this;
        if (!player.level().isClientSide || !DoubucklerHands.holdingInMain(player)) {
            return;
        }
        cir.setReturnValue(DoubucklerHands.displayOffhand(player));
    }
}
