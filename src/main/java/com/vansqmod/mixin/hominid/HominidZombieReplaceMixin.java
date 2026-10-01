package com.vansqmod.mixin.hominid;

import com.vansqmod.config.BlockedEntityConfig;
import com.vansqmod.entity.BoulderingZombie;
import com.vansqmod.entity.Putrid;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Hominid replaces a fraction of vanilla {@code Zombie} finalizations with Juggernaut/Bellman via
 * {@code discard()} + {@code addFreshEntity()}. If the replacement id is blocked, keep the original
 * zombie instead of deleting it and spawning the replacement.
 */
@Mixin(targets = "com.alganaut.hominid.registry.event.HominidClientEvents", remap = false)
public abstract class HominidZombieReplaceMixin {

    @Unique
    private static final ThreadLocal<Mob> vansqmod$pendingOriginal = new ThreadLocal<>();

    @Redirect(
            method = "onEntityJoinWorld",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;discard()V", remap = true),
            remap = false
    )
    private static void vansqmod$deferDiscard(Mob mob) {
        vansqmod$pendingOriginal.set(mob);
    }

    @Redirect(
            method = "onEntityJoinWorld",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", remap = true),
            remap = false
    )
    private static boolean vansqmod$filterReplacement(Level level, Entity replacement) {
        Mob original = vansqmod$pendingOriginal.get();
        vansqmod$pendingOriginal.remove();
        if (replacement == original) {
            return true;
        }
        if (original instanceof Putrid || original instanceof BoulderingZombie) {
            if (replacement != null && !replacement.isRemoved()) {
                replacement.discard();
            }
            return false;
        }
        if (BlockedEntityConfig.isBlocked(replacement)) {
            if (!replacement.isRemoved()) {
                replacement.discard();
            }
            return false;
        }
        if (replacement.isRemoved()) {
            return false;
        }
        if (original != null && !original.isRemoved()) {
            original.discard();
        }
        return level.addFreshEntity(replacement);
    }
}
