package com.vansqmod.entity;

import com.vansqmod.compat.GelidSnowballs;
import com.vansqmod.registry.ModEntityTypes;
import com.vansqmod.registry.ModParticleTypes;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Crossbow projectile for {@code yungscavebiomes:icicle}, matching Jaden's Nether Expansion
 * {@code netherexp:black_icicle}. Stats are named constants so they can be tuned later.
 */
public class IcicleProjectile extends AbstractArrow {

    public static final ResourceLocation ICICLE_ID =
            ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "icicle");

    /** Arrow default is 0.05. Black icicles use 0.15 (triple drop). */
    public static final double GRAVITY = 0.15D;
    /** Arrow default is 2.0; full-charge bow velocity 3 yields 6 damage. */
    public static final double BASE_DAMAGE = 2.0D;

    /**
     * Freeze ticks applied on hit (refreshed, not stacked).
     * Vanilla freeze damage starts at 140 ticks.
     */
    public static final int FREEZE_TICKS = 60;

    public static final int SHATTER_PARTICLES = 8;
    public static final float SHATTER_VOLUME = 1.0F;
    public static final float SHATTER_PITCH = 1.4F;

    public IcicleProjectile(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        this.setSoundEvent(SoundEvents.GLASS_BREAK);
        this.setBaseDamage(BASE_DAMAGE);
    }

    public IcicleProjectile(Level level, LivingEntity shooter, ItemStack pickup, ItemStack weapon) {
        super(ModEntityTypes.ICICLE.get(), shooter, level, pickup, weapon);
        this.setSoundEvent(SoundEvents.GLASS_BREAK);
        this.setBaseDamage(BASE_DAMAGE);
    }

    public static boolean isIcicleAmmo(ItemStack stack) {
        return !stack.isEmpty() && ICICLE_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide && !this.inGround) {
            this.level().addParticle(ModParticleTypes.ICE_FLAKE.get(), this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.shatterIcicle();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity hit = result.getEntity();
        if (!this.level().isClientSide) {
            GelidSnowballs.addFreeze(hit, FREEZE_TICKS);
        }
        this.shatterIcicle();
    }

    @Override
    protected double getDefaultGravity() {
        return GRAVITY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(BuiltInRegistries.ITEM.getOptional(ICICLE_ID).orElse(Items.ICE));
    }

    private void shatterIcicle() {
        this.playSound(SoundEvents.GLASS_BREAK, SHATTER_VOLUME, SHATTER_PITCH);
        if (this.level() instanceof ServerLevel serverLevel) {
            Block block = BuiltInRegistries.BLOCK.getOptional(ICICLE_ID).orElse(Blocks.ICE);
            BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, block.defaultBlockState());
            for (int i = 0; i < SHATTER_PARTICLES; i++) {
                serverLevel.sendParticles(particle, this.getX(), this.getY(), this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                serverLevel.sendParticles(
                        ModParticleTypes.ICE_FLAKE.get(),
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        1,
                        0.15D,
                        0.15D,
                        0.15D,
                        0.0D
                );
            }
        }
        this.discard();
    }
}
