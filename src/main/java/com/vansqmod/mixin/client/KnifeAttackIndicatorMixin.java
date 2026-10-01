package com.vansqmod.mixin.client;

import com.vansqmod.compat.SweepingTagHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Knives recharge in a single tick, so the crosshair/hotbar attack indicator flashes
 * every swing. Force a full charge while a knife is held; scythes keep the vanilla bar.
 */
@Mixin(Gui.class)
public abstract class KnifeAttackIndicatorMixin {

    @Redirect(
            method = "renderCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"
            ),
            require = 0
    )
    private float vansqmod$hideKnifeCrosshairIndicatorLocal(LocalPlayer player, float partialTick) {
        return knifeCharge(player, partialTick);
    }

    @Redirect(
            method = "renderCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getAttackStrengthScale(F)F"
            ),
            require = 0
    )
    private float vansqmod$hideKnifeCrosshairIndicator(Player player, float partialTick) {
        return knifeCharge(player, partialTick);
    }

    @Redirect(
            method = "renderItemHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getAttackStrengthScale(F)F"
            ),
            require = 0
    )
    private float vansqmod$hideKnifeHotbarIndicator(Player player, float partialTick) {
        return knifeCharge(player, partialTick);
    }

    private static float knifeCharge(Player player, float partialTick) {
        if (SweepingTagHandler.isKnifeWeapon(player.getMainHandItem())) {
            return 1.0F;
        }
        return player.getAttackStrengthScale(partialTick);
    }
}
