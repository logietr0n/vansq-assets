package com.vansqmod.mixin.caverns;

import com.vansqmod.integration.caverns.DontMoveAdvancement;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * C&amp;C grants Don't. Move. as soon as a Peeper sets its target. Gate it to a Peeper
 * that is currently targeting the player and within 32 blocks.
 */
@Mixin(
        targets = "com.teamabnormals.caverns_and_chasms.common.entity.monster.creeper.Peeper",
        remap = false
)
public abstract class PeeperDontMoveAdvancementMixin {

    @Redirect(
            method = "setTarget",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancements/critereon/PlayerTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;)V",
                    remap = true
            ),
            remap = false
    )
    private void vansqmod$requireNearbyTarget(PlayerTrigger trigger, ServerPlayer player) {
        DontMoveAdvancement.tryGrant((Entity) (Object) this, player);
    }

    @Inject(method = "tick", at = @At("TAIL"), remap = true)
    private void vansqmod$grantWhenCloseAndTargeting(CallbackInfo ci) {
        LivingEntity target = ((Mob) (Object) this).getTarget();
        DontMoveAdvancement.tryGrant((Entity) (Object) this, target);
    }
}
