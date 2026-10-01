package com.vansqmod.mixin.borninchaos;

import com.vansqmod.integration.missionaryhat.MissionaryHatEquipment;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Blocks vanilla helmet-slot equip ({@code ArmorItem#use} / dispensers). Curios handles
 * equip-from-use into {@code head}; armor slots reject it via {@link ArmorSlotMissionaryHatMixin}.
 */
@Mixin(ArmorItem.class)
public abstract class MissionaryHatArmorItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void vansqmod$useHeadCurioSlot(
            Level level,
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir
    ) {
        if (!MissionaryHatEquipment.isHatItem((Item) (Object) this)) {
            return;
        }
        ItemStack stack = player.getItemInHand(hand);
        cir.setReturnValue(InteractionResultHolder.pass(stack));
    }

    @Inject(method = "getEquipmentSlot", at = @At("HEAD"), cancellable = true)
    private void vansqmod$notHelmetEquipment(CallbackInfoReturnable<EquipmentSlot> cir) {
        if (MissionaryHatEquipment.isHatItem((Item) (Object) this)) {
            cir.setReturnValue(EquipmentSlot.MAINHAND);
        }
    }
}
