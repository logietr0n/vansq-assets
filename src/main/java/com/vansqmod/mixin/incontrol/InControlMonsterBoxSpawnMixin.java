package com.vansqmod.mixin.incontrol;

import com.vansqmod.compat.MonsterBoxRework;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "mcjty.incontrol.ForgeEventHandlers", remap = false)
public abstract class InControlMonsterBoxSpawnMixin {

    @Redirect(
            method = {
                    "onPositionCheck",
                    "onEntitySpawnEvent",
                    "onEntityJoinWorld"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lmcjty/incontrol/rules/SpawnRule;getResult()Lmcjty/incontrol/rules/support/ICResult;"
            )
    )
    private @Coerce Object vansqmod$ignoreBoxDenies(@Coerce Object rule) {
        try {
            Object result = rule.getClass().getMethod("getResult").invoke(rule);
            return MonsterBoxRework.ignoreInControlDeny(result);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
