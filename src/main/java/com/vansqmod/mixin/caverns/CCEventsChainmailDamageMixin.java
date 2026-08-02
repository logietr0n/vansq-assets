package com.vansqmod.mixin.caverns;

import com.vansqmod.integration.caverns.CavernsArmorAttributeOverrides;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Collapses duplicate attack-damage modifiers right after C&amp;C's {@code onItemModify} runs.
 */
@Mixin(targets = "com.teamabnormals.caverns_and_chasms.core.other.CCEvents", remap = false)
public final class CCEventsChainmailDamageMixin {

    @Inject(method = "onItemModify", at = @At("RETURN"))
    private static void vansqmod$enforceArmorAttackDamage(ItemAttributeModifierEvent event, CallbackInfo ci) {
        CavernsArmorAttributeOverrides.enforceAttackDamagePerPiece(event);
    }
}
