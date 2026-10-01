package com.vansqmod.mixin.yungscavebiomes;

import com.vansqmod.block.FrostLilyMultifaceBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Lets a frost lily replace creeping ice in the same cell, so you can plant on
 * a wall that already has an ice sheet instead of fighting it for the space.
 */
@Mixin(
        targets = "com.yungnickyoung.minecraft.yungscavebiomes.block.IceSheetBlock",
        remap = false
)
public abstract class IceSheetFrostLilyReplaceMixin extends MultifaceBlock {

    public IceSheetFrostLilyReplaceMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (context.getItemInHand().getItem() instanceof BlockItem item
                && item.getBlock() instanceof FrostLilyMultifaceBlock) {
            return true;
        }
        return super.canBeReplaced(state, context);
    }
}
