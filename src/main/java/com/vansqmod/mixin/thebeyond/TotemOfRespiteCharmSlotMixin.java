package com.vansqmod.mixin.thebeyond;

import com.vansqmod.integration.charm.CharmTotemEquipment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Totem of Respite also consumes a Charm-slot stack, not only hands.
 */
@Mixin(targets = "com.thebeyond.common.event.ModGameEvents", remap = false)
public abstract class TotemOfRespiteCharmSlotMixin {

    @Redirect(
            method = "onDeath",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;",
                    remap = true
            ),
            remap = false
    )
    private static ItemStack vansqmod$mainHandOrCharm(Player player) {
        ItemStack main = player.getMainHandItem();
        if (CharmTotemEquipment.isRespite(main) || CharmTotemEquipment.isRespite(player.getOffhandItem())) {
            return main;
        }
        return CharmTotemEquipment.findCharmRespite(player).orElse(main);
    }
}
