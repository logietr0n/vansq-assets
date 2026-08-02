package com.vansqmod.mixin.backpacks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vansqmod.integration.backpacks.BackpackEquipment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the vanilla cape when a backpack is on Curios {@code back} (Backpacks' own mixin only checks chest).
 */
@Mixin(CapeLayer.class)
public final class BackpackCapeLayerMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void vansqmod$hideCapeWhenBackpackOnBack(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer livingEntity,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        if (BackpackEquipment.hasBackpackEquipped(livingEntity)) {
            ci.cancel();
        }
    }
}
