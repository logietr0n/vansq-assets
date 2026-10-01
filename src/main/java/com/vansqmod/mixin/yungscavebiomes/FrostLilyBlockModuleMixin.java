package com.vansqmod.mixin.yungscavebiomes;

import com.vansqmod.block.FrostLilyMultifaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * YUNG registers frost lily as a {@code BushBlock}. Redirect that supplier so the
 * same registry id is a multiface plant that can attach to walls and ceilings.
 */
@Mixin(
        targets = "com.yungnickyoung.minecraft.yungscavebiomes.module.BlockModule",
        remap = false
)
public abstract class FrostLilyBlockModuleMixin {

    @Inject(method = "lambda$static$3", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$multifaceFrostLily(CallbackInfoReturnable<Block> cir) {
        cir.setReturnValue(new FrostLilyMultifaceBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.ICE)
                        .noOcclusion()
                        .noCollission()
                        .instabreak()
                        .dynamicShape()
                        .pushReaction(PushReaction.DESTROY)
                        .lightLevel(state -> 10)
                        .sound(SoundType.GLASS)
        ));
    }
}
