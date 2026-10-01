package com.vansqmod.block;

import com.vansqmod.compat.SoulFirePlacer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Spreads and survives like regular fire, but always stays soul fire visually
 * and deals soul-fire damage. Used by soul fireball impact/spread.
 */
public class ForcedSoulFireBlock extends FireBlock {

    public ForcedSoulFireBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        SoulFirePlacer.run(() -> super.tick(state, level, pos, random));
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        SoulFirePlacer.run(() -> super.randomTick(state, level, pos, random));
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!entity.fireImmune()) {
            entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 1);
            if (entity.getRemainingFireTicks() == 0) {
                entity.igniteForSeconds(8.0F);
            }
            entity.hurt(level.damageSources().inFire(), 2.0F);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(24) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.BLOCKS,
                    1.0F + random.nextFloat(),
                    random.nextFloat() * 0.7F + 0.3F,
                    false
            );
        }
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        if (!this.canBurn(belowState) && !belowState.isFaceSturdy(level, below, Direction.UP)) {
            if (this.canBurn(level.getBlockState(pos.west()))) {
                spawnSoulFlame(level, pos, random, 0.05D, 0.5D, random.nextDouble());
            }
            if (this.canBurn(level.getBlockState(pos.east()))) {
                spawnSoulFlame(level, pos, random, 0.95D, 0.5D, random.nextDouble());
            }
            if (this.canBurn(level.getBlockState(pos.north()))) {
                spawnSoulFlame(level, pos, random, random.nextDouble(), 0.5D, 0.05D);
            }
            if (this.canBurn(level.getBlockState(pos.south()))) {
                spawnSoulFlame(level, pos, random, random.nextDouble(), 0.5D, 0.95D);
            }
        } else {
            for (int i = 0; i < 3; i++) {
                spawnSoulFlame(level, pos, random, random.nextDouble(), random.nextDouble() * 0.5D + 0.5D, random.nextDouble());
            }
        }
    }

    private static void spawnSoulFlame(
            Level level,
            BlockPos pos,
            RandomSource random,
            double xOff,
            double yOff,
            double zOff
    ) {
        level.addParticle(
                ParticleTypes.SOUL_FIRE_FLAME,
                pos.getX() + xOff,
                pos.getY() + yOff,
                pos.getZ() + zOff,
                0.0D,
                0.0D,
                0.0D
        );
        if (random.nextInt(2) == 0) {
            level.addParticle(
                    ParticleTypes.SOUL,
                    pos.getX() + xOff,
                    pos.getY() + yOff,
                    pos.getZ() + zOff,
                    0.0D,
                    0.02D,
                    0.0D
            );
        }
    }
}
