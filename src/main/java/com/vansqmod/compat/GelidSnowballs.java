package com.vansqmod.compat;

import com.vansqmod.entity.IceSnowballProjectile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Gelid offhand snowballs, plus freeze/damage for vanilla and ice snowballs.
 */
public final class GelidSnowballs {

    public static final ResourceLocation GELID_ID = FrostedCavesGelidConversion.GELID_ID;

    public static final int SNOWBALL_FREEZE_TICKS = 60;
    public static final int SNOWBALL_GELID_FREEZE_TICKS = 120;
    public static final float SNOWBALL_GELID_DAMAGE = 1.0F;

    public static final int ICE_FREEZE_TICKS = 240;
    public static final int ICE_GELID_FREEZE_TICKS = 480;
    public static final float ICE_DAMAGE = 2.0F;
    public static final float ICE_GELID_DAMAGE = 4.0F;

    public static final float ICE_HOLD_CHANCE = 0.05F;
    public static final float ICE_HOLD_CHANCE_FROSTED_CAVES = 0.20F;

    private GelidSnowballs() {
    }

    public static boolean isGelid(Entity entity) {
        if (entity == null) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return GELID_ID.equals(id);
    }

    public static boolean isIceSnowball(ItemStack stack) {
        return IceSnowballProjectile.isAmmo(stack);
    }

    public static boolean isThrowableSnowball(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(Items.SNOWBALL) || isIceSnowball(stack));
    }

    public static ItemStack iceSnowballStack() {
        Item item = BuiltInRegistries.ITEM.get(IceSnowballProjectile.ITEM_ID);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    public static void addFreeze(Entity entity, int ticks) {
        if (ticks <= 0 || entity.isOnFire() || !entity.canFreeze()) {
            return;
        }
        // Refresh instead of stacking. Unbounded add() keeps the powder-snow overlay
        // at 100% long after icy/slowness has worn off.
        entity.setTicksFrozen(Math.max(entity.getTicksFrozen(), ticks));
    }

    public static void setFreeze(Entity entity, int ticks) {
        if (ticks <= 0 || entity.isOnFire() || !entity.canFreeze()) {
            return;
        }
        entity.setTicksFrozen(ticks);
    }
}
