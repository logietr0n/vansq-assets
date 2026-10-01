package com.vansqmod.mixin;

import com.vansqmod.debug.VansqDebugState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * Backs {@code /vansq debug equip}. Vanilla
 * {@code Mob#populateDefaultEquipmentSlots} gates armor behind
 * {@code nextFloat() < 0.15 * specialMultiplier}, then uses later
 * {@code nextFloat()} calls for tier upgrades and "stop adding pieces".
 *
 * <p>Only that gate is forced. Returning {@code 0} for the float is not enough:
 * {@code 0 < 0.15 * 0} is false when regional difficulty has not ramped up yet
 * (new worlds, spawn chunks, Easy). The float is forced below zero and the
 * multiplier is floored at 1 so the original inner loop still runs — leather /
 * gold / chain / iron / diamond (and copper / rose-gold remaps) keep their
 * armored-spawn ratios.</p>
 */
@Mixin(Mob.class)
public abstract class VansqDebugForceEquipMixin {

    @Redirect(
            method = "populateDefaultEquipmentSlots",
            slice = @Slice(
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/DifficultyInstance;getSpecialMultiplier()F"
                    )
            ),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextFloat()F")
    )
    private float vansqmod$forceArmorGate(RandomSource random) {
        return VansqDebugState.isForceEquipmentEnabled() ? -1.0F : random.nextFloat();
    }

    @Redirect(
            method = "populateDefaultEquipmentSlots",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/DifficultyInstance;getSpecialMultiplier()F"
            )
    )
    private float vansqmod$forceArmorMultiplier(DifficultyInstance difficulty) {
        float multiplier = difficulty.getSpecialMultiplier();
        if (VansqDebugState.isForceEquipmentEnabled()) {
            return Math.max(multiplier, 1.0F);
        }
        return multiplier;
    }
}
