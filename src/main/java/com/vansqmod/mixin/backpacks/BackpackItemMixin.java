package com.vansqmod.mixin.backpacks;

import com.spydnel.backpacks.common.items.BackpackItem;
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
 * Prevents the backpack from using vanilla chest-slot equip ({@code swapWithEquipmentSlot}).
 * Curios handles equip-from-use into the {@code back} slot; armor slots reject it via
 * {@link ArmorSlotBackpackMixin}.
 */
@Mixin(BackpackItem.class)
public abstract class BackpackItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void vansqmod$useBackCurioSlot(
            Level level,
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir
    ) {
        ItemStack stack = player.getItemInHand(hand);
        // Pass so Curios RightClickItem remains the equip path; do not swap into CHEST.
        cir.setReturnValue(InteractionResultHolder.pass(stack));
        cir.cancel();
    }

    /**
     * Stop advertising the backpack as chest armor to vanilla equip helpers (dispensers, etc.).
     * MAINHAND is never auto-equipped as armor.
     */
    @Inject(method = "getEquipmentSlot", at = @At("HEAD"), cancellable = true)
    private void vansqmod$notChestEquipment(CallbackInfoReturnable<EquipmentSlot> cir) {
        cir.setReturnValue(EquipmentSlot.MAINHAND);
    }
}
