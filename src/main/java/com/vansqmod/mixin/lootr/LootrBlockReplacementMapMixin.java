package com.vansqmod.mixin.lootr;

import com.vansqmod.compat.LootrChestBarrelOnly;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lootr's replacement map is the conversion choke point (chunk tick, structure
 * loot unpack, etc.). Skip anything that is not a chest or barrel.
 */
@Mixin(targets = "noobanidus.mods.lootr.common.api.replacement.BlockReplacementMap", remap = false)
public abstract class LootrBlockReplacementMapMixin {

    @Inject(method = "getReplacement", at = @At("HEAD"), cancellable = true, remap = false)
    private void vansqmod$chestsAndBarrelsOnly(BlockState state, CallbackInfoReturnable<BlockState> cir) {
        if (!LootrChestBarrelOnly.allowsConversion(state)) {
            cir.setReturnValue(null);
        }
    }
}
