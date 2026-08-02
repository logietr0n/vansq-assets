package com.vansqmod.mixin.toolbelt;

import com.vansqmod.integration.toolbelt.ToolbeltEnchantmentSupport;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Removes leggings armor from the toolbelt item data; block reach is applied through Curios when worn on {@code belt}.
 */
@Mixin(targets = "com.teamabnormals.caverns_and_chasms.common.item.ToolbeltItem", remap = false)
public final class ToolbeltItemAttributesMixin {

    @Inject(method = "getDefaultAttributeModifiers", at = @At("HEAD"), cancellable = true)
    private void vansqmod$noArmorOnlyTooltipRange(
            ItemStack stack,
            CallbackInfoReturnable<ItemAttributeModifiers> cir
    ) {
        var builder = ItemAttributeModifiers.builder()
                .add(
                        Attributes.BLOCK_INTERACTION_RANGE,
                        new AttributeModifier(
                                ResourceLocation.withDefaultNamespace("armor.leggings"),
                                2.0F,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.ANY
                );

        int extending = ToolbeltEnchantmentSupport.getExtendingLevel(stack);
        if (extending > 0) {
            builder.add(
                    Attributes.BLOCK_INTERACTION_RANGE,
                    new AttributeModifier(
                            ResourceLocation.fromNamespaceAndPath("vansqmod", "toolbelt_extending"),
                            (float) ToolbeltEnchantmentSupport.extendingRangeBonus(extending),
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.ANY
            );
        }

        cir.setReturnValue(builder.build());
    }
}
