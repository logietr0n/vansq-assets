package com.vansqmod.entity;

import com.vansqmod.compat.SoulFirePlacer;
import com.vansqmod.registry.ModEntityTypes;
import com.vansqmod.registry.ModItems;
import com.vansqmod.registry.ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Bow/crossbow/thrown soul fire charge. Fire placed or spread from this projectile
 * is always {@code vansqmod:soul_fire}.
 */
public class SoulFireballProjectile extends AbstractArrow {

    public static final double GRAVITY = 0.05D;
    public static final double BASE_DAMAGE = 5.0D;
    public static final float THROW_SPEED = 1.1F;
    public static final int THROW_COOLDOWN = 10;
    public static final float IGNITE_SECONDS = 5.0F;
    public static final int FIRE_RADIUS = 1;

    public SoulFireballProjectile(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        this.setBaseDamage(BASE_DAMAGE);
        this.pickup = Pickup.DISALLOWED;
        this.setSoundEvent(SoundEvents.FIRECHARGE_USE);
    }

    public SoulFireballProjectile(Level level, LivingEntity shooter, ItemStack pickup) {
        this(level, shooter, pickup, null);
    }

    public SoulFireballProjectile(Level level, LivingEntity shooter, ItemStack pickup, ItemStack weapon) {
        super(ModEntityTypes.SOUL_FIRE_CHARGE.get(), shooter, level, pickup, sanitizeWeapon(weapon));
        this.setBaseDamage(BASE_DAMAGE);
        this.pickup = Pickup.DISALLOWED;
        this.setSoundEvent(SoundEvents.FIRECHARGE_USE);
    }

    public SoulFireballProjectile(Level level, double x, double y, double z, ItemStack pickup, ItemStack weapon) {
        super(ModEntityTypes.SOUL_FIRE_CHARGE.get(), x, y, z, level, pickup, sanitizeWeapon(weapon));
        this.setBaseDamage(BASE_DAMAGE);
        this.pickup = Pickup.DISALLOWED;
        this.setSoundEvent(SoundEvents.FIRECHARGE_USE);
    }

    public static boolean isAmmo(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModItems.SOUL_FIRE_CHARGE.get());
    }

    /** Thrown/dispensed arrows must not pass {@link ItemStack#EMPTY} as the firing weapon. */
    private static ItemStack sanitizeWeapon(ItemStack weapon) {
        return weapon == null || weapon.isEmpty() ? null : weapon;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide && !this.inGround) {
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            this.level().addParticle(ModParticleTypes.SOUL_FIREBALL_TRAIL.get(), this.getX(), this.getY() + 0.1D, this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity hit = result.getEntity();
        hit.igniteForSeconds(IGNITE_SECONDS);
        this.igniteArea(BlockPos.containing(result.getLocation()));
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.playSound(SoundEvents.FIRECHARGE_USE, 1.0F, 1.0F);
        this.igniteArea(result.getBlockPos().relative(result.getDirection()));
        this.discard();
    }

    @Override
    protected double getDefaultGravity() {
        return GRAVITY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.SOUL_FIRE_CHARGE.get());
    }

    private void igniteArea(BlockPos center) {
        if (this.level().isClientSide || !this.canGrief()) {
            return;
        }
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-FIRE_RADIUS, -FIRE_RADIUS, -FIRE_RADIUS),
                center.offset(FIRE_RADIUS, FIRE_RADIUS, FIRE_RADIUS)
        )) {
            SoulFirePlacer.place(this.level(), pos.immutable());
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    center.getX() + 0.5D,
                    center.getY() + 0.5D,
                    center.getZ() + 0.5D,
                    12,
                    0.35D,
                    0.35D,
                    0.35D,
                    0.02D
            );
        }
    }

    private boolean canGrief() {
        Entity owner = this.getOwner();
        if (owner instanceof Player) {
            return true;
        }
        return this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }
}
