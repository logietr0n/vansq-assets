package com.vansqmod.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Barched kinetic spear damage uses {@link LivingEntity#getAttributeBaseValue}, which ignores item attack
 * modifiers, so tooltip damage and stab damage disagree. Use {@link LivingEntity#getAttributeValue} for
 * normal spears. Silver spear uses Caverns &amp; Chasms magic damage for the kinetic base instead.
 */
@Mixin(targets = "net.minecraft.world.item.component.KineticWeapon")
public class KineticWeaponMixin {

    private static final ResourceLocation SILVER_SPEAR = ResourceLocation.withDefaultNamespace("silver_spear");
    private static final ResourceLocation CAVERNS_MAGIC_DAMAGE =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "magic_damage");

    @Unique
    private static final ThreadLocal<ItemStack> VANSQMOD_KINETIC_WEAPON_STACK = new ThreadLocal<>();

    @Inject(method = "damageEntities", at = @At("HEAD"), require = 0)
    private void vansqmod$captureKineticStack(
            ItemStack itemStack,
            int useTicksRemaining,
            LivingEntity livingEntity,
            EquipmentSlot equipmentSlot,
            CallbackInfo ci
    ) {
        VANSQMOD_KINETIC_WEAPON_STACK.set(itemStack);
    }

    @Inject(method = "damageEntities", at = @At("RETURN"), require = 0)
    private void vansqmod$clearKineticStack(
            ItemStack itemStack,
            int useTicksRemaining,
            LivingEntity livingEntity,
            EquipmentSlot equipmentSlot,
            CallbackInfo ci
    ) {
        VANSQMOD_KINETIC_WEAPON_STACK.remove();
    }

    @Redirect(
            method = "damageEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getAttributeBaseValue(Lnet/minecraft/core/Holder;)D"
            ),
            require = 0
    )
    private double vansqmod$kineticDamageBase(LivingEntity livingEntity, Holder<Attribute> attribute) {
        ItemStack stack = VANSQMOD_KINETIC_WEAPON_STACK.get();
        if (stack != null && isSilverSpear(stack)) {
            return BuiltInRegistries.ATTRIBUTE.getHolder(CAVERNS_MAGIC_DAMAGE)
                    .map(livingEntity::getAttributeValue)
                    .orElse(0.0D);
        }
        return livingEntity.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    private static boolean isSilverSpear(ItemStack stack) {
        return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(SILVER_SPEAR);
    }
}
