package com.vansqmod.mixin.caverns;

import com.vansqmod.integration.tetherpotion.TetherPotionEquipment;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents wearable potions from using vanilla helmet-slot equip ({@code swapWithEquipmentSlot}).
 * Curios handles equip-from-use into {@code head}; armor slots reject them via
 * {@link ArmorSlotTetherPotionMixin}.
 */
@Mixin(targets = "com.teamabnormals.caverns_and_chasms.common.item.TetherPotionItem", remap = false)
public abstract class TetherPotionItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true, remap = true)
    private void vansqmod$useHeadCurioSlot(
            Level level,
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir
    ) {
        ItemStack stack = player.getItemInHand(hand);
        cir.setReturnValue(InteractionResultHolder.pass(stack));
    }

    @Inject(
            method = "getEquipmentSlot(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/EquipmentSlot;",
            at = @At("HEAD"),
            cancellable = true,
            remap = true
    )
    private void vansqmod$notHelmetEquipment(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> cir) {
        if (TetherPotionEquipment.isPotion(stack)) {
            cir.setReturnValue(EquipmentSlot.MAINHAND);
        }
    }
}
