package com.vansqmod.mixin.client;

import com.vansqmod.client.QuarkAttributeTooltipFormat;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Quark attribute tooltip text follows {@link net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation},
 * not {@code attribute_tooltips.json} display types. Delegates formatting to
 * {@link QuarkAttributeTooltipFormat} (no synthetics on this mixin class).
 */
@Mixin(targets = "org.violetmoon.quark.content.client.tooltip.AttributeTooltips", remap = false)
public final class QuarkAttributeTooltipsMixin {

    @Inject(method = "format", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$formatAttributeModifiers(
            ItemAttributeModifiers.Entry entry,
            double baseVal,
            CallbackInfoReturnable<MutableComponent> cir
    ) {
        try {
            MutableComponent formatted = QuarkAttributeTooltipFormat.tryFormat(entry, baseVal);
            if (formatted != null) {
                cir.setReturnValue(formatted);
            }
        } catch (Throwable ignored) {
            // Never let tooltip formatting take down FancyMenu / screen render.
        }
    }
}
