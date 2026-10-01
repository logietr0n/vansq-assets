package com.vansqmod.mixin.separatedleaves;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Separated Leaves calls {@code getStructureWithPieceAt} on every leaf distance update.
 * Pairing still uses biome + datapack rules; skip the structure walk.
 */
@Mixin(StructureManager.class)
public abstract class SeparatedLeavesStructureScanMixin {

    @Inject(
            method = "getStructureWithPieceAt(Lnet/minecraft/core/BlockPos;Lnet/minecraft/tags/TagKey;)Lnet/minecraft/world/level/levelgen/structure/StructureStart;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void vansqmod$skipMismatchedLeavesScan(
            BlockPos pos,
            TagKey<?> tag,
            CallbackInfoReturnable<StructureStart> cir
    ) {
        if (tag != null && "separatedleaves".equals(tag.location().getNamespace())) {
            cir.setReturnValue(StructureStart.INVALID_START);
        }
    }
}
