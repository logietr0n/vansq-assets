package com.vansqmod.mixin.caverns;

import com.vansqmod.integration.tetherpotion.TetherPotionEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes C&amp;C tether / impact / trail potion tick, break, and unequip logic read the Curios
 * {@code head} slot instead of vanilla helmet.
 */
@Mixin(targets = "com.teamabnormals.caverns_and_chasms.core.other.CCEvents", remap = false)
public final class CCEventsTetherPotionMixin {

    /**
     * Skip C&amp;C's helmet-unequip effect strip while the potion still lives in Curios
     * (helmet → curio migration would otherwise convert infinite tether effects to a short remainder).
     */
    @Inject(method = "onEquipmentChange", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$skipHelmetUnequipIfCurio(LivingEquipmentChangeEvent event, CallbackInfo ci) {
        if (event.getSlot() != EquipmentSlot.HEAD || !TetherPotionEquipment.isPotion(event.getFrom())) {
            return;
        }
        if (TetherPotionEquipment.getCuriosPotion(event.getEntity()).isPresent()) {
            ci.cancel();
        }
    }

    /**
     * Third {@code getItemBySlot} in {@code onLivingTick} is the wearable-potion check.
     * The earlier two are copper weathering / copper-helmet lightning and must keep vanilla armor.
     */
    @Redirect(
            method = "onLivingTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;",
                    remap = true,
                    ordinal = 2
            ),
            remap = false
    )
    private static ItemStack vansqmod$tickPotionFromCurios(LivingEntity entity, EquipmentSlot slot) {
        return TetherPotionEquipment.resolveWornPotion(entity, slot);
    }

    @Redirect(
            method = "onLivingDamagePost",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;",
                    remap = true
            ),
            remap = false
    )
    private static ItemStack vansqmod$breakPotionFromCurios(LivingEntity entity, EquipmentSlot slot) {
        return TetherPotionEquipment.resolveWornPotion(entity, slot);
    }

    @Redirect(
            method = "onLivingDamagePost",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V",
                    remap = true
            ),
            remap = false
    )
    private static void vansqmod$clearPotionCurioOnBreak(LivingEntity entity, EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.HEAD && stack.isEmpty()
                && TetherPotionEquipment.getCuriosPotion(entity).isPresent()) {
            TetherPotionEquipment.setEquippedPotion(entity, ItemStack.EMPTY);
            return;
        }
        entity.setItemSlot(slot, stack);
    }
}
