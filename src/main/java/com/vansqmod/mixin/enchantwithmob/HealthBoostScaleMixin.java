package com.vansqmod.mixin.enchantwithmob;

import baguchi.enchantwithmob.mobenchant.HealthBoostMobEnchant;
import baguchi.enchantwithmob.mobenchant.MobEnchant;
import com.vansqmod.VansqMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Titanic (Health Boost) also grows the mob by 3% scale per level.
 */
@Mixin(value = HealthBoostMobEnchant.class, remap = false)
public abstract class HealthBoostScaleMixin {

    private static final double SCALE_PER_LEVEL = 0.03D;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void vansqmod$addTitanicScale(MobEnchant.Properties properties, CallbackInfo ci) {
        ((MobEnchant) (Object) this).addAttributesModifier(
                Attributes.SCALE,
                ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "mob_enchant.health_boost.scale"),
                SCALE_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
