package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import com.vansqmod.entity.IcicleProjectile;
import com.vansqmod.entity.SoulFireballProjectile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.lang.reflect.Constructor;

public final class RangedAmmo {

    private static final ResourceLocation BLACK_ICICLE_ID =
            ResourceLocation.fromNamespaceAndPath("netherexp", "black_icicle");

    /** Full-charge bow velocity is 3, so {@code ceil(base * 3) == 8}. */
    public static final double BLACK_ICICLE_BASE_DAMAGE = 8.0D / 3.0D;

    private static final Constructor<?> BLACK_ICICLE_CTOR = findBlackIcicleCtor();

    private RangedAmmo() {
    }

    public static boolean isAmmo(ItemStack stack) {
        return IcicleProjectile.isIcicleAmmo(stack)
                || isBlackIcicleAmmo(stack)
                || SoulFireballProjectile.isAmmo(stack);
    }

    public static boolean isBlackIcicleAmmo(ItemStack stack) {
        return BLACK_ICICLE_CTOR != null
                && !stack.isEmpty()
                && BLACK_ICICLE_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static Projectile create(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo) {
        ItemStack projectile = ammo.copyWithCount(1);
        Projectile spawned = null;
        if (IcicleProjectile.isIcicleAmmo(ammo)) {
            spawned = new IcicleProjectile(level, shooter, projectile, weapon);
        } else if (isBlackIcicleAmmo(ammo)) {
            spawned = createBlackIcicle(level, shooter, projectile, weapon);
        } else if (SoulFireballProjectile.isAmmo(ammo)) {
            spawned = new SoulFireballProjectile(level, shooter, projectile, weapon);
        }
        if (spawned instanceof AbstractArrow arrow) {
            applyBowDamage(arrow, ammo);
        }
        return spawned;
    }

    public static void applyCrit(Projectile projectile, ItemStack ammo, boolean crit) {
        if (!crit || !(projectile instanceof AbstractArrow arrow) || isIcicleLike(ammo)) {
            return;
        }
        arrow.setCritArrow(true);
    }

    private static boolean isIcicleLike(ItemStack ammo) {
        return IcicleProjectile.isIcicleAmmo(ammo) || isBlackIcicleAmmo(ammo);
    }

    private static void applyBowDamage(AbstractArrow arrow, ItemStack ammo) {
        if (IcicleProjectile.isIcicleAmmo(ammo)) {
            arrow.setBaseDamage(IcicleProjectile.BASE_DAMAGE);
            arrow.setCritArrow(false);
        } else if (isBlackIcicleAmmo(ammo)) {
            arrow.setBaseDamage(BLACK_ICICLE_BASE_DAMAGE);
            arrow.setCritArrow(false);
        }
    }

    private static Projectile createBlackIcicle(
            Level level,
            LivingEntity shooter,
            ItemStack projectile,
            ItemStack weapon
    ) {
        try {
            return (Projectile) BLACK_ICICLE_CTOR.newInstance(level, shooter, projectile, weapon);
        } catch (ReflectiveOperationException e) {
            VansqMod.LOGGER.warn("Failed to create netherexp black icicle projectile", e);
            return null;
        }
    }

    private static Constructor<?> findBlackIcicleCtor() {
        try {
            Constructor<?> ctor = Class.forName("net.jadenxgamer.netherexp.core.entity.BlackIcicle")
                    .getConstructor(Level.class, LivingEntity.class, ItemStack.class, ItemStack.class);
            ctor.setAccessible(true);
            return ctor;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
