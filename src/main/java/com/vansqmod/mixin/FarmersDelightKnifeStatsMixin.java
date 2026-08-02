package com.vansqmod.mixin;

import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "vectorwing.farmersdelight.common.registry.ModItems", remap = false)
public class FarmersDelightKnifeStatsMixin {
    @Inject(method = "knifeItem", at = @At("HEAD"), cancellable = true)
    private static void vansqmod$knifeItemOverride(Tier tier, CallbackInfoReturnable<Item.Properties> cir) {
        float desiredDamage;

        // Farmer's Delight Flint tier is an anonymous Tier implementation. We can't reference it directly
        // without a compile-time dependency, so we detect it by its unique stats.
        boolean isFarmersDelightFlintTier =
                tier != null
                        && tier != Tiers.WOOD
                        && tier != Tiers.STONE
                        && tier != Tiers.IRON
                        && tier != Tiers.DIAMOND
                        && tier != Tiers.GOLD
                        && tier != Tiers.NETHERITE
                        && tier.getUses() == 131
                        && tier.getEnchantmentValue() == 5
                        && tier.getSpeed() == 4.0F
                        && tier.getAttackDamageBonus() == 1.0F;

        // Caller-built knives only (see {@link com.vansqmod.compat.AbnormalsDelightNecromiumKnifeCompat}). AD registers
        // necromium knife without calling this helper.
        // BlueprintItemTier NECROMIUM: durability 2031, damage bonus 3. Vanilla Netherite also uses 2031 but bonus 4.
        boolean isNecromiumKnifeTier =
                tier != null
                        && tier != Tiers.DIAMOND
                        && tier != Tiers.NETHERITE
                        && tier.getUses() == 2031
                        && tier.getAttackDamageBonus() == 3.0F
                        && tier.getEnchantmentValue() == 15
                        && tier.getSpeed() == 9.0F;

        if (isFarmersDelightFlintTier) {
            desiredDamage = 0.5F;
        } else if (tier == Tiers.IRON) {
            desiredDamage = 1.0F;
        } else if (tier == Tiers.DIAMOND || isNecromiumKnifeTier) {
            desiredDamage = 1.5F;
        } else if (tier == Tiers.NETHERITE) {
            desiredDamage = 1.75F;
        } else if (tier == Tiers.GOLD) {
            desiredDamage = 0.5F;
        } else {
            // Any other knife tiers keep their existing FD values.
            return;
        }

        // Final Attack Damage for tools is: 1.0 (base) + tierBonus + itemDamageModifier
        // We want the *final* damage to match desiredDamage, so subtract the tier bonus.
        float attackDamageModifier = desiredDamage - 1.0F - tier.getAttackDamageBonus();
        float attackSpeedModifier = 10.0F - 4.0F; // -> 6.0F

        cir.setReturnValue(new Item.Properties().attributes(DiggerItem.createAttributes(tier, attackDamageModifier, attackSpeedModifier)));
    }
}

