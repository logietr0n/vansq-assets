package com.vansqmod.entity;

import com.vansqmod.compat.GelidSnowballs;
import com.vansqmod.registry.ModEntityTypes;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Replaces {@code spider_overhaul:ice_snowball}'s projectile with Amendments snowball
 * behavior (freeze, trail, 3D texture). Impact uses the same snowflake burst as a
 * vanilla snowball, plus item-break crumbs from the ice snowball texture.
 */
public class IceSnowballProjectile extends AbstractArrow {

    public static final ResourceLocation ITEM_ID =
            ResourceLocation.fromNamespaceAndPath("spider_overhaul", "ice_snowball");

    /** Vanilla throwable snowball gravity. Ice snowball used 0.04. */
    public static final double GRAVITY = 0.03D;

    public static final int FREEZE_TICKS = GelidSnowballs.ICE_FREEZE_TICKS;
    public static final int GELID_FREEZE_TICKS = GelidSnowballs.ICE_GELID_FREEZE_TICKS;
    public static final float DAMAGE = GelidSnowballs.ICE_DAMAGE;
    public static final float GELID_DAMAGE = GelidSnowballs.ICE_GELID_DAMAGE;

    public static final float THROW_SPEED = 1.5F;
    public static final float THROW_INACCURACY = 1.0F;
    public static final int IMPACT_PARTICLES = 8;

    public IceSnowballProjectile(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        this.setBaseDamage(DAMAGE);
        this.pickup = Pickup.DISALLOWED;
        this.setSoundEvent(SoundEvents.SNOW_BREAK);
    }

    public IceSnowballProjectile(Level level, LivingEntity shooter, ItemStack pickup) {
        this(level, shooter, pickup, null);
    }

    public IceSnowballProjectile(Level level, LivingEntity shooter, ItemStack pickup, ItemStack weapon) {
        super(ModEntityTypes.ICE_SNOWBALL.get(), shooter, level, pickup, sanitizeWeapon(weapon));
        this.setBaseDamage(DAMAGE);
        this.pickup = Pickup.DISALLOWED;
        this.setSoundEvent(SoundEvents.SNOW_BREAK);
    }

    public IceSnowballProjectile(Level level, double x, double y, double z, ItemStack pickup, ItemStack weapon) {
        super(ModEntityTypes.ICE_SNOWBALL.get(), x, y, z, level, pickup, sanitizeWeapon(weapon));
        this.setBaseDamage(DAMAGE);
        this.pickup = Pickup.DISALLOWED;
        this.setSoundEvent(SoundEvents.SNOW_BREAK);
    }

    public static boolean isAmmo(ItemStack stack) {
        return !stack.isEmpty() && ITEM_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    /** Thrown/dispensed arrows must not pass {@link ItemStack#EMPTY} as the firing weapon. */
    private static ItemStack sanitizeWeapon(ItemStack weapon) {
        return weapon == null || weapon.isEmpty() ? null : weapon;
    }

    public static void applySnowballFreeze(Entity entity) {
        GelidSnowballs.addFreeze(entity, FREEZE_TICKS);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isOnFire() && !this.level().isClientSide) {
            this.playEntityOnFireExtinguishedSound();
            this.discard();
            return;
        }
        if (this.level().isClientSide && !this.inGround && !this.isInWater() && this.random.nextFloat() >= 0.85F) {
            this.level().addParticle(
                    ParticleTypes.SNOWFLAKE,
                    this.getX() + this.random.triangle(-0.2D, 0.2D),
                    this.getY() + 0.1D + this.random.triangle(-0.2D, 0.2D),
                    this.getZ() + this.random.triangle(-0.2D, 0.2D),
                    this.random.nextGaussian() * 0.015D,
                    this.random.nextGaussian() * 0.015D,
                    this.random.nextGaussian() * 0.015D
            );
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hit = result.getEntity();
        Entity owner = this.getOwner();
        boolean gelid = GelidSnowballs.isGelid(owner);
        float damage = gelid ? GELID_DAMAGE : DAMAGE;
        hit.hurt(this.damageSources().thrown(this, owner), damage);
        if (!this.level().isClientSide) {
            GelidSnowballs.addFreeze(hit, gelid ? GELID_FREEZE_TICKS : FREEZE_TICKS);
        }
        this.poofAndDiscard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.poofAndDiscard();
    }

    @Override
    protected double getDefaultGravity() {
        return GRAVITY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(BuiltInRegistries.ITEM.getOptional(ITEM_ID).orElse(Items.SNOWBALL));
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ParticleOptions crumbs = new ItemParticleOption(ParticleTypes.ITEM, this.breakItem());
            for (int i = 0; i < IMPACT_PARTICLES; i++) {
                this.level().addParticle(
                        ParticleTypes.SNOWFLAKE,
                        this.getRandomX(1.0D),
                        this.getRandomY(),
                        this.getRandomZ(1.0D),
                        this.random.nextGaussian() * 0.035D,
                        this.random.nextGaussian() * 0.015D * 0.02D,
                        this.random.nextGaussian() * 0.035D
                );
                this.level().addParticle(crumbs, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
            return;
        }
        super.handleEntityEvent(id);
    }

    private ItemStack breakItem() {
        ItemStack stack = this.getPickupItem();
        return stack.isEmpty() ? this.getDefaultPickupItem() : stack;
    }

    private void poofAndDiscard() {
        this.playSound(SoundEvents.SNOW_BREAK, 1.0F, 1.0F);
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }
}
