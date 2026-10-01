package com.vansqmod.mixin.quark;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "org.violetmoon.quark.content.world.block.MonsterBoxBlock", remap = false)
public abstract class MonsterBoxHardnessMixin {

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;strength(F)Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;",
                    remap = true
            )
    )
    private static float vansqmod$hardness(float original) {
        return 10.0F;
    }
}
