package com.vansqmod.mixin.borninchaos;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Missionary dark-charge shots ignore undead collision so they pass through summons
 * and can still hit the player on the same path.
 */
@Mixin(Projectile.class)
public abstract class MissionaryChargeUndeadMixin {

    private static final ResourceLocation MISSIONARY_CHARGE =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "missionary_charge");
    private static final ResourceLocation RESTLESS_SPIRIT =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "restless_spirit");

    @Inject(method = "canHitEntity", at = @At("HEAD"), cancellable = true)
    private void vansqmod$skipUndead(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        Projectile self = (Projectile) (Object) this;
        if (!MISSIONARY_CHARGE.equals(BuiltInRegistries.ENTITY_TYPE.getKey(self.getType()))) {
            return;
        }
        if (entity instanceof Player) {
            return;
        }
        if (entity instanceof LivingEntity living && isUndead(living)) {
            cir.setReturnValue(false);
        }
    }

    private static boolean isUndead(LivingEntity living) {
        return living.getType().is(EntityTypeTags.UNDEAD)
                || living.isInvertedHealAndHarm()
                || RESTLESS_SPIRIT.equals(BuiltInRegistries.ENTITY_TYPE.getKey(living.getType()));
    }
}
