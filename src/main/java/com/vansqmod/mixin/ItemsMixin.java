package com.vansqmod.mixin;

import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import zzik2.barched.bridge.item.Item$PropertiesBridge;
import zzik2.barched.minecraft.world.item.SpearItem;
import zzik2.zreflex.mixin.ModifyAccess;

import com.vansqmod.registry.ModItems;
import com.vansqmod.registry.ModTiers;
import zzik2.barched.Barched;

@Mixin(Items.class)
public abstract class ItemsMixin {

    /** Same kinetic tuning as Barched {@code diamond_spear} ({@code ItemsMixin} in Barched). */
    private static final float DIAMOND_SPEAR_DAMAGE_MULT = 1.075F;
    private static final float DIAMOND_SPEAR_DELAY = 0.5F;
    private static final float DIAMOND_SPEAR_DISMOUNT_MAX_SEC = 3.0F;
    private static final float DIAMOND_SPEAR_DISMOUNT_MIN_SPEED = 7.5F;
    private static final float DIAMOND_SPEAR_KNOCKBACK_MAX_SEC = 6.5F;
    private static final float DIAMOND_SPEAR_KNOCKBACK_MIN_SPEED = 5.1F;
    private static final float DIAMOND_SPEAR_DAMAGE_MAX_SEC = 10.0F;
    private static final float DIAMOND_SPEAR_DAMAGE_MIN_REL_SPEED = 4.6F;

    @Shadow
    public static Item registerItem(String id, Item item) {
        return null;
    }

    /**
     * {@link Item$PropertiesBridge} is applied at runtime by Barched; cast is safe once mixins load.
     * Barched 0.0.12+ exposes {@code spear(Tier, ...)} (not the older {@code spear(Tiers, ...)}).
     */
    @SuppressWarnings("unchecked")
    private static Item$PropertiesBridge newSpearPropertiesBridge() {
        return (Item$PropertiesBridge) (Object) new Item.Properties();
    }

    private static Item.Properties spearProps(Tier tier, float attackSpeed, float damageMult) {
        return newSpearPropertiesBridge().spear(
                tier,
                attackSpeed,
                damageMult,
                DIAMOND_SPEAR_DELAY,
                DIAMOND_SPEAR_DISMOUNT_MAX_SEC,
                DIAMOND_SPEAR_DISMOUNT_MIN_SPEED,
                DIAMOND_SPEAR_KNOCKBACK_MAX_SEC,
                DIAMOND_SPEAR_KNOCKBACK_MIN_SPEED,
                DIAMOND_SPEAR_DAMAGE_MAX_SEC,
                DIAMOND_SPEAR_DAMAGE_MIN_REL_SPEED
        );
    }

    @ModifyAccess(access = Opcodes.ACC_PUBLIC)
    private static final Item ROSE_GOLD_SPEAR = registerItem(
            "rose_gold_spear",
            new SpearItem(
                    ModTiers.ROSE_GOLD,
                    // Barched 0.0.12 accepts {@link Tier}; rose-gold attack bonus comes from the tier (2.5).
                    // 1 / 1.0 - 4 = -3 attack speed modifier → 4 + (-3) = 1.0 attacks/sec
                    spearProps(ModTiers.ROSE_GOLD, 1.0F, DIAMOND_SPEAR_DAMAGE_MULT)
                            // Rose Gold spear durability between iron (250) and diamond (1561)
                            .durability(383)
            )
    );

    @ModifyAccess(access = Opcodes.ACC_PUBLIC)
    private static final Item SILVER_SPEAR = registerItem(
            "silver_spear",
            new SpearItem(
                    ModTiers.SILVER,
                    // Keep gold-tier attack bonus (0) for pack balance; swing speed matches golden spear (0.95).
                    // Kinetic tuning matches diamond spear so velocity stab scaling matches.
                    spearProps(Tiers.GOLD, 0.95F, DIAMOND_SPEAR_DAMAGE_MULT)
                            // Use Caverns & Chasms silver durability.
                            .durability(157)
                            // Make the spear's kinetic stab damage use vanilla magic damage type.
                            // This preserves Barched's speed-based damage scaling, but applies it as magic damage.
                            .component(Barched.DataComponents.DAMAGE_TYPE, new EitherHolder<DamageType>(DamageTypes.MAGIC))
            )
    );

    /** Same offensive stats as diamond spear; necromium durability + CC slowness infliction via tag merge. */
    @ModifyAccess(access = Opcodes.ACC_PUBLIC)
    private static final Item NECROMIUM_SPEAR = registerItem(
            "necromium_spear",
            new SpearItem(
                    ModTiers.NECROMIUM,
                    // Diamond attack bonus (3.0) matches {@link ModTiers#NECROMIUM}.
                    spearProps(Tiers.DIAMOND, 1.05F, DIAMOND_SPEAR_DAMAGE_MULT).durability(2031)
            )
    );

    static {
        ModItems.ROSE_GOLD_SPEAR = ROSE_GOLD_SPEAR;
        ModItems.SILVER_SPEAR = SILVER_SPEAR;
        ModItems.NECROMIUM_SPEAR = NECROMIUM_SPEAR;
    }
}
