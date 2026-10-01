package com.vansqmod.mixin;

import com.vansqmod.registry.ModEntityTags;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Snowy creepers and other {@code #vansqmod:not_monsters} types still use Monster
 * AI, but should not block sleeping like hostile mobs do.
 */
@Mixin(Monster.class)
public abstract class MonsterNotMonsterRestMixin {

    @Inject(method = "isPreventingPlayerRest", at = @At("HEAD"), cancellable = true)
    private void vansqmod$notMonstersDoNotBlockSleep(Player player, CallbackInfoReturnable<Boolean> cir) {
        Monster self = (Monster) (Object) this;
        if (self.getType().is(ModEntityTags.NOT_MONSTERS)) {
            cir.setReturnValue(false);
        }
    }
}
