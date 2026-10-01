package com.vansqmod.mixin.sleeptight;

import com.vansqmod.compat.SleepTightDoubleBedSplit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Before Sleep Tight handles an occupied double bed, move the current user
 * onto the bed that was not clicked so the clicker can lie in the one they chose.
 */
@Mixin(targets = "net.mehvahdjukaar.sleep_tight.core.ModEvents", remap = false)
public abstract class SleepTightDoubleBedSplitMixin {

    @Inject(method = "onRightClickBlock", at = @At("HEAD"))
    private static void vansqmod$splitDoubleBed(
            Player player,
            Level level,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        SleepTightDoubleBedSplit.onBedClick(player, level, hand, hit);
    }
}
