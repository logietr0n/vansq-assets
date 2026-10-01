package com.vansqmod.mixin.hominid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Mellified splash used Honeyed amplifier 10 (Honeyed XI, 11 HP/s). Use amplifier 0 (Honeyed I).
 */
@Mixin(targets = "com.alganaut.hominid.registry.entity.custom.Mellified", remap = false)
public abstract class MellifiedHoneyedMixin {

    @ModifyConstant(method = "healZombies", constant = @Constant(intValue = 10))
    private int vansqmod$honeyedAmplifierZero(int original) {
        return 0;
    }
}
