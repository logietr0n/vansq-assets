package com.vansqmod.mixin.peaceless;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Disables Peaceless Mimic natural spawning from loot chests.
 * Structure chests use {@code #peaceless:spawns_mimics}; dungeon chests use a
 * {@code CONTAINS_MIMIC} attachment — both funnel through this method.
 */
@Mixin(targets = "com.ninni.peaceless.common.entity.Mimic", remap = false)
public final class MimicSpawnMixin {

    @Inject(method = "trySpawnMimicFromContainer", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$disableNaturalMimicSpawns(
            LevelAccessor level,
            BlockPos pos,
            BlockEntity be,
            CallbackInfoReturnable<Boolean> cir
    ) {
        cir.setReturnValue(false);
    }
}
