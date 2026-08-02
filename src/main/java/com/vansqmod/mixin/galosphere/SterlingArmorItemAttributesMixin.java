package com.vansqmod.mixin.galosphere;

import com.vansqmod.VansqMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hardcoded Sterling armor stats (replaces AttributeSetter datapack entries).
 * No Illager Resistance; armor/toughness match former AttributeSetter totals.
 * <p>
 * Helmet/boots 2 armor, chestplate 6, leggings 5; +2 toughness each.
 */
@Mixin(targets = "net.orcinus.galosphere.items.SterlingArmorItem", remap = false)
public abstract class SterlingArmorItemAttributesMixin {

    private static final ResourceLocation TOUGHNESS_ID =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "sterling_toughness");

    @Inject(method = "getDefaultAttributeModifiers", at = @At("HEAD"), cancellable = true)
    private void vansqmod$sterlingAttributes(CallbackInfoReturnable<ItemAttributeModifiers> cir) {
        ArmorItem armor = (ArmorItem) (Object) this;
        ArmorItem.Type type = armor.getType();
        EquipmentSlotGroup slot = EquipmentSlotGroup.bySlot(type.getSlot());
        ResourceLocation armorId = ResourceLocation.withDefaultNamespace("armor." + type.getName());

        cir.setReturnValue(ItemAttributeModifiers.builder()
                .add(
                        Attributes.ARMOR,
                        new AttributeModifier(armorId, armorPoints(type), Operation.ADD_VALUE),
                        slot
                )
                .add(
                        Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(TOUGHNESS_ID, 2.0D, Operation.ADD_VALUE),
                        slot
                )
                .build());
    }

    /** Galosphere 1.5.5 NeoForge uses a static helper for the Illager damage reduction. */
    @Inject(
            method = "getIllagerResistance(Lnet/minecraft/world/entity/EquipmentSlot;)F",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void vansqmod$noIllagerResistance(EquipmentSlot slot, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(0.0F);
    }

    private static double armorPoints(ArmorItem.Type type) {
        return switch (type) {
            case HELMET, BOOTS -> 2.0D;
            case CHESTPLATE -> 6.0D;
            case LEGGINGS -> 5.0D;
            case BODY -> 3.0D;
        };
    }
}
