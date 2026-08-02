package com.vansqmod.mixin.toolbelt;

import com.vansqmod.integration.toolbelt.ToolbeltEquipment;
import com.vansqmod.integration.toolbelt.ToolbeltSkySetSupport;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Toolbelt does not override {@link Item#use} (unlike Backpacks' backpack item), so we hook {@link Item#use}
 * and filter by the held stack. Blocks vanilla legs-slot equip; Curios handles belt-slot equip-from-use.
 */
@Mixin(Item.class)
public abstract class ToolbeltItemUseMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void vansqmod$toolbeltUse(
            Level level,
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir
    ) {
        ItemStack held = player.getItemInHand(hand);
        Item item = (Item) (Object) this;

        if (ToolbeltSkySetSupport.trySkySetUse(item, level, player, hand, cir::setReturnValue)) {
            cir.cancel();
            return;
        }

        if (!ToolbeltEquipment.isToolbelt(held)) {
            return;
        }

        cir.setReturnValue(InteractionResultHolder.success(held));
        cir.cancel();
    }
}
