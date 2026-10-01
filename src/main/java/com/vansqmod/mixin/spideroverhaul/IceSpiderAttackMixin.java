package com.vansqmod.mixin.spideroverhaul;

import com.vansqmod.compat.SpiderOverhaulCombat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Ice Spider melee icy duration is 60 ticks (3 seconds). Use 10 seconds.
 */
@Mixin(targets = "dev.chybx.spideroverhaul.entity.IceSpiderEntity", remap = false)
public abstract class IceSpiderAttackMixin {

    @ModifyConstant(method = "onAttackSuccess", constant = @Constant(intValue = 60))
    private int vansqmod$icyTenSeconds(int original) {
        return SpiderOverhaulCombat.ICY_DURATION_TICKS;
    }
}
