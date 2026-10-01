package com.vansqmod.mixin.alexsmobs;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Alex's Mobs cockroaches use the walked-on block's {@code SoundType} footsteps.
 * Born in Chaos baby spiders play {@code entity.spider.step}; cockroaches use
 * the same event at volume 0.02.
 */
@Mixin(targets = "com.github.alexthe666.alexsmobs.entity.EntityCockroach")
public abstract class EntityCockroachStepSoundMixin extends Animal {

    protected EntityCockroachStepSoundMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.02F, 1.0F);
    }
}
