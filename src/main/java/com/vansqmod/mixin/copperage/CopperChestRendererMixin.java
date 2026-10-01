package com.vansqmod.mixin.copperage;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Item-form copper chests are dummy block entities with no level. Their packed light is often
 * left at world/zero values, so the inventory model renders unlit compared to vanilla chests.
 */
@Mixin(targets = "com.github.smallinger.copperagebackport.client.renderer.CopperChestRenderer", remap = false)
public abstract class CopperChestRendererMixin {

    @ModifyVariable(
            method = "render(Lnet/minecraft/world/level/block/entity/ChestBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            remap = true
    )
    private int vansqmod$itemFormFullBright(int packedLight, ChestBlockEntity chest) {
        if (chest != null && chest.getLevel() == null) {
            return LightTexture.FULL_BRIGHT;
        }
        return packedLight;
    }
}
