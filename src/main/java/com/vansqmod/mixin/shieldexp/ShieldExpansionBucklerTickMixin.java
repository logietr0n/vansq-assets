package com.vansqmod.mixin.shieldexp;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.shieldexp.access.LivingEntityAccess;
import org.infernalstudios.shieldexp.events.ShieldEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tallestred.piglinproliferation.common.items.BucklerItem;

/**
 * Shield Expansion's {@code onStartUsing} cancels <em>any</em> item that is on
 * cooldown, including Piglin Proliferation bucklers after a shield disable or
 * leftover SE cooldown. Its player tick can also call {@code stopUsingItem()}
 * when a leftover {@code blocking} flag is set and the current item is not an
 * SE shield. Keep the 10-tick charge windup on {@link BucklerItem} only.
 */
@Mixin(value = ShieldEvents.class, remap = false)
public abstract class ShieldExpansionBucklerTickMixin {

    @Inject(method = "onStartUsing", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$allowBucklerStart(Entity entity, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!(stack.getItem() instanceof BucklerItem)) {
            return;
        }
        if (entity instanceof Player player) {
            LivingEntityAccess.get(player).shieldexp$setBlocking(false);
            LivingEntityAccess.get(player).shieldexp$setParryWindow(0);
        }
        cir.setReturnValue(false);
    }

    @Inject(method = "onStopUsing", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$skipBucklerStop(Entity entity, ItemStack stack, CallbackInfo ci) {
        if (stack.getItem() instanceof BucklerItem) {
            ci.cancel();
        }
    }

    @Inject(method = "onUseTick", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$skipBucklerUseTick(Entity entity, ItemStack stack, CallbackInfo ci) {
        if (stack.getItem() instanceof BucklerItem) {
            ci.cancel();
        }
    }

    @Inject(method = "onPlayerTick", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$skipSeTickWhileUsingBuckler(Player player, CallbackInfo ci) {
        if (player.getUseItem().getItem() instanceof BucklerItem) {
            LivingEntityAccess.get(player).shieldexp$setBlocking(false);
            LivingEntityAccess.get(player).shieldexp$setParryWindow(0);
            ci.cancel();
        }
    }
}
