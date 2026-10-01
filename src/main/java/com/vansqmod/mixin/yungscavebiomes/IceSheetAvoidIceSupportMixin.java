package com.vansqmod.mixin.yungscavebiomes;

import com.vansqmod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Creeping ice worldgen skips neighbors in {@code ice_sheet_feature_avoid}
 * ({@code #minecraft:ice}). Spreading still uses {@code getStateForPlacement},
 * which only checks a sturdy face — so it can climb onto permafrost even when
 * that block is in the ice tag. Refuse ice, Quark permafrost, and permafrost
 * palladium ore so generation and spread match.
 */
@Mixin(
        targets = "com.yungnickyoung.minecraft.yungscavebiomes.block.IceSheetBlock",
        remap = false
)
public abstract class IceSheetAvoidIceSupportMixin {

    @Inject(
            method = "getStateForPlacement(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void vansqmod$skipIceAndPermafrostSupports(
            BlockState current,
            BlockGetter level,
            BlockPos pos,
            Direction direction,
            CallbackInfoReturnable<BlockState> cir
    ) {
        String threadName = Thread.currentThread().getName();
        if (threadName.startsWith("DH-")
                || threadName.contains("DistantHorizons")
                || threadName.contains("LOD World Gen")) {
            return;
        }
        if (vansqmod$isIceSheetAvoidSupport(level.getBlockState(pos.relative(direction)))) {
            cir.setReturnValue(null);
        }
    }

    @Unique
    private static boolean vansqmod$isIceSheetAvoidSupport(BlockState support) {
        if (support.is(BlockTags.ICE) || support.is(ModBlocks.PERMAFROST_PALLADIUM_ORE.get())) {
            return true;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(support.getBlock());
        return "quark".equals(id.getNamespace()) && id.getPath().startsWith("permafrost");
    }
}
