package com.vansqmod.mixin.incubation;

import com.teamabnormals.incubation.common.block.BirdNestBlock;
import com.teamabnormals.incubation.common.entity.ai.goal.LayEggInNestGoal;
import com.vansqmod.entity.RareChickenVariants;
import com.vansqmod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LayEggInNestGoal.class)
public abstract class RareChickenLayEggMixin {

    @Shadow
    @Final
    private Animal bird;

    @ModifyArg(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/teamabnormals/incubation/common/block/EmptyNestBlock;getNest(Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/level/block/Block;",
                    remap = false
            )
    )
    private Item vansqmod$rareNestEgg(Item egg) {
        return RareChickenVariants.isRare(bird) ? ModItems.RARE_EGG.get() : egg;
    }

    @Inject(method = "isValidTarget", at = @At("RETURN"), cancellable = true)
    private void vansqmod$rareNestTarget(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() || !RareChickenVariants.isRare(bird)) {
            return;
        }
        BlockState state = level.getBlockState(pos.above());
        if (state.getBlock() instanceof BirdNestBlock nest
                && nest.getEgg() == ModItems.RARE_EGG.get()
                && state.getValue(BirdNestBlock.EGGS) < 6) {
            cir.setReturnValue(true);
        }
    }
}
