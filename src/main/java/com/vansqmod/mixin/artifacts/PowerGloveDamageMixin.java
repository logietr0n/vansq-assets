package com.vansqmod.mixin.artifacts;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Artifacts Power Glove is {@code +4} {@link AttributeModifier.Operation#ADD_VALUE}.
 * Apply {@code +20%} of base attack damage instead, including the item tooltip.
 */
@Mixin(targets = "artifacts.component.ability.AttributeModifiers$Entry", remap = false)
public abstract class PowerGloveDamageMixin {

    private static final double DAMAGE_MULTIPLIER = 0.2D;

    @Unique
    private static Object percentAmount;

    @Shadow
    public abstract ResourceLocation id();

    @Inject(method = "amount", at = @At("HEAD"), cancellable = true)
    private void vansqmod$percentAmount(CallbackInfoReturnable<Object> cir) {
        if (isPowerGlove()) {
            cir.setReturnValue(percentAmount());
        }
    }

    @Inject(method = "operation", at = @At("HEAD"), cancellable = true)
    private void vansqmod$percentOperation(CallbackInfoReturnable<AttributeModifier.Operation> cir) {
        if (isPowerGlove()) {
            cir.setReturnValue(AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }
    }

    @Unique
    private boolean isPowerGlove() {
        ResourceLocation id = this.id();
        return "artifacts".equals(id.getNamespace()) && id.getPath().startsWith("power_glove");
    }

    @Unique
    private static Object percentAmount() {
        if (percentAmount == null) {
            percentAmount = constantDoubleValue(DAMAGE_MULTIPLIER);
        }
        return percentAmount;
    }

    @Unique
    private static Object constantDoubleValue(double value) {
        try {
            Class<?> valueClass = Class.forName("artifacts.config.value.Value");
            return valueClass.getMethod("of", Object.class).invoke(null, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create Artifacts Value constant", e);
        }
    }
}
