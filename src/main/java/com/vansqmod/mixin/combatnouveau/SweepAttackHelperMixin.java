package com.vansqmod.mixin.combatnouveau;

import com.vansqmod.compat.SweepingTagHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Air-sweep path from Combat Nouveau.
 * <p>
 * Tagged weapons may air-sweep without Sweeping Edge and deal full attack damage.
 * The looked-at entity is excluded so CN's air-sweep packet (sent on every full-strength
 * click) does not stack with {@link net.minecraft.world.entity.player.Player#attack}.
 */
@Mixin(targets = "fuzs.combatnouveau.helper.SweepAttackHelper", remap = false)
public abstract class SweepAttackHelperMixin {

    @Inject(method = "allowSweepAttack", at = @At("HEAD"), cancellable = true, remap = false)
    private static void vansqmod$restrictAirSweep(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (SweepingTagHandler.isSweepingWeapon(player.getMainHandItem())) {
            cir.setReturnValue(true);
            return;
        }
        // Non-tagged: keep enchant-gated air sweeps only (no free sword sweeps).
        cir.setReturnValue(SweepingTagHandler.hasSweepingEdge(player));
    }

    @Inject(method = "sweepAttack", at = @At("HEAD"), cancellable = true, remap = false)
    private static void vansqmod$fullAirSweepDamage(
            Player player,
            AABB aABB,
            float attackDamage,
            Entity target,
            CallbackInfo ci
    ) {
        if (!SweepingTagHandler.isSweepingWeapon(player.getMainHandItem())) {
            return;
        }

        // Air sweeps pass target == null; exclude the crosshair entity so it only takes the melee hit.
        Entity excluded = target != null ? target : SweepingTagHandler.findLookTarget(player);
        SweepingTagHandler.performFullDamageSweep(player, aABB, excluded);
        ci.cancel();
    }
}
