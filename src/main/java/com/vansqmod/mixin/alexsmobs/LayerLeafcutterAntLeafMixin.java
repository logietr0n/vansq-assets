package com.vansqmod.mixin.alexsmobs;

import com.github.alexthe666.alexsmobs.entity.EntityLeafcutterAnt;
import com.vansqmod.compat.LeafcutterAntLeafColors;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Alex's Mobs 1.22.x ports {@code LayerLeafcutterAntLeaf} to the 1.21
 * {@code renderToBuffer(..., int color)} signature but still passes {@code -1}
 * (opaque white). The RGB of the bitten leaf is computed and then discarded.
 */
@Mixin(
        targets = "com.github.alexthe666.alexsmobs.client.render.layer.LayerLeafcutterAntLeaf",
        remap = false
)
public abstract class LayerLeafcutterAntLeafMixin {

    @ModifyArgs(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILcom/github/alexthe666/alexsmobs/entity/EntityLeafcutterAnt;FFFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/alexthe666/citadel/client/model/AdvancedEntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
            ),
            require = 0
    )
    private void vansqmod$tintHeldLeaf(
            Args args,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            EntityLeafcutterAnt ant,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        args.set(4, LeafcutterAntLeafColors.packedArgb(ant));
    }
}
