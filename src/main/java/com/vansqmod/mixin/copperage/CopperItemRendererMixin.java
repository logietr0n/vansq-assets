package com.vansqmod.mixin.copperage;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Copper chest items use a block-entity renderer instead of the baked chest model, so GUI
 * lighting never matches vanilla chests. Force fullbright in inventory / item-frame contexts.
 */
@Mixin(targets = "com.github.smallinger.copperagebackport.client.renderer.CopperItemRenderer", remap = false)
public abstract class CopperItemRendererMixin {

    @ModifyVariable(method = "renderByItem", at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = true)
    private int vansqmod$guiChestLight(
            int packedLight,
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource bufferSource
    ) {
        if (displayContext == ItemDisplayContext.GUI || displayContext == ItemDisplayContext.FIXED) {
            return LightTexture.FULL_BRIGHT;
        }
        return packedLight;
    }
}
