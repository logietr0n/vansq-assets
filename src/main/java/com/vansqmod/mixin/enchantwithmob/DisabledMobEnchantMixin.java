package com.vansqmod.mixin.enchantwithmob;

import baguchi.enchantwithmob.utils.MobEnchantUtils;
import baguchi.enchantwithmob.utils.MobEnchantmentData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Honors EnchantWithMob's {@code disableMobEnchants} list when rolling
 * spawn and loot enchants. The config flag is otherwise unused by 21.3.0.
 */
@Mixin(value = MobEnchantUtils.class, remap = false)
public abstract class DisabledMobEnchantMixin {

    @Inject(method = "getAvailableEnchantmentResults", at = @At("RETURN"))
    private static void vansqmod$skipDisabledEnchants(
            int level,
            java.util.stream.Stream<?> possibleEnchantments,
            CallbackInfoReturnable<List<MobEnchantmentData>> cir) {
        List<MobEnchantmentData> list = cir.getReturnValue();
        if (list == null || list.isEmpty()) {
            return;
        }
        list.removeIf(data -> data == null
                || data.enchantment == null
                || !data.enchantment.isBound()
                || data.enchantment.value().isDisabled());
    }
}
