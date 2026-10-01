package com.vansqmod.mixin.enchantwithmob;

import baguchi.enchantwithmob.mobenchant.MobEnchant;
import baguchi.enchantwithmob.mobenchant.SpeedyMobEnchant;
import com.vansqmod.VansqMod;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Nimble (Speedy): +4% movement speed and −3% scale per level, max V.
 */
@Mixin(value = SpeedyMobEnchant.class, remap = false)
public abstract class SpeedyScaleMixin extends MobEnchant {

    private static final double SPEED_PER_LEVEL = 0.04D;
    private static final double SCALE_PER_LEVEL = -0.03D;

    public SpeedyScaleMixin(MobEnchant.Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public MobEnchant addAttributesModifier(
            Holder<Attribute> attribute,
            ResourceLocation id,
            double amount,
            AttributeModifier.Operation operation
    ) {
        if (attribute.is(Attributes.MOVEMENT_SPEED)) {
            amount = SPEED_PER_LEVEL;
        }
        return super.addAttributesModifier(attribute, id, amount, operation);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void vansqmod$addNimbleScale(MobEnchant.Properties properties, CallbackInfo ci) {
        this.addAttributesModifier(
                Attributes.SCALE,
                ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "mob_enchant.speedy.scale"),
                SCALE_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
