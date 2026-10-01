package com.vansqmod.mixin.quark;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Quark permafrost uses stone sounds. Frosted Caves treats it as the deepslate
 * analogue, so steps/break/place should match deepslate.
 */
@Mixin(BlockBehaviour.class)
public abstract class PermafrostSoundTypeMixin {

    @Inject(method = "getSoundType", at = @At("HEAD"), cancellable = true)
    private void vansqmod$permafrostDeepslateSounds(BlockState state, CallbackInfoReturnable<SoundType> cir) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if ("quark".equals(id.getNamespace()) && id.getPath().startsWith("permafrost")) {
            cir.setReturnValue(SoundType.DEEPSLATE);
        }
    }
}
