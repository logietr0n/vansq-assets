package com.vansqmod.mixin.caverns;

import com.vansqmod.VansqMod;
import com.vansqmod.integration.caverns.CavernsArmorAttributeOverrides;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Sanguine armor: chainmail defense, +5% attack damage per piece, no toughness, lifesteal, and per-piece max health.
 */
@Mixin(targets = "com.teamabnormals.caverns_and_chasms.common.item.SanguineArmorItem", remap = false)
public abstract class SanguineArmorItemAttributesMixin {

    private static final ResourceLocation CAVERNS_LIFESTEAL =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "lifesteal");

    @Inject(method = "getDefaultAttributeModifiers", at = @At("HEAD"), cancellable = true)
    private void vansqmod$sanguineAttributes(ItemStack stack, CallbackInfoReturnable<ItemAttributeModifiers> cir) {
        ArmorItem armor = (ArmorItem) (Object) this;
        ArmorItem.Type type = armor.getType();
        EquipmentSlotGroup slot = EquipmentSlotGroup.bySlot(type.getSlot());
        ResourceLocation armorId = ResourceLocation.withDefaultNamespace("armor." + type.getName());

        var builder = ItemAttributeModifiers.builder()
                .add(
                        Attributes.ARMOR,
                        new AttributeModifier(armorId, chainmailDefense(type), Operation.ADD_VALUE),
                        slot
                )
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                CavernsArmorAttributeOverrides.vansqAttackDamageId(type, "sanguine"),
                                CavernsArmorAttributeOverrides.ATTACK_DAMAGE_PERCENT_PER_PIECE,
                                Operation.ADD_MULTIPLIED_BASE
                        ),
                        slot
                );

        float healthBonus = maxHealthBonus(type);
        if (healthBonus > 0.0F) {
            // Unique id per slot so modifiers stack (same id = only one piece applies).
            ResourceLocation healthId = ResourceLocation.fromNamespaceAndPath(
                    VansqMod.MODID,
                    "sanguine_health." + type.getName()
            );
            builder.add(
                    Attributes.MAX_HEALTH,
                    new AttributeModifier(healthId, healthBonus, Operation.ADD_VALUE),
                    slot
            );
        }

        var lifesteal = BuiltInRegistries.ATTRIBUTE.getHolder(CAVERNS_LIFESTEAL).orElse(null);
        if (lifesteal != null) {
            builder.add(
                    lifesteal,
                    new AttributeModifier(armorId, 0.05D, Operation.ADD_MULTIPLIED_BASE),
                    slot
            );
        }

        cir.setReturnValue(builder.build());
    }

    private static int chainmailDefense(ArmorItem.Type armorType) {
        return switch (armorType) {
            case BOOTS -> 1;
            case LEGGINGS -> 4;
            case CHESTPLATE -> 5;
            case HELMET -> 2;
            case BODY -> 3;
            default -> 0;
        };
    }

    private static float maxHealthBonus(ArmorItem.Type armorType) {
        return switch (armorType) {
            case HELMET -> 2.0F;
            case CHESTPLATE -> 3.0F;
            case LEGGINGS -> 3.0F;
            case BOOTS -> 2.0F;
            default -> 0.0F;
        };
    }
}
