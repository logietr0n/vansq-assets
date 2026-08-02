package com.vansqmod.item;

import com.vansqmod.compat.RhodoheartSoundTypes;
import com.vansqmod.registry.ModParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RhodoheartShardItem extends Item {

    private static final float HEAL_AMOUNT = 2.0F;
    private static final int HEAL_PARTICLE_COUNT = 8;

    public RhodoheartShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!canUseOn(target)) {
            return InteractionResult.FAIL;
        }

        if (target.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        target.heal(HEAL_AMOUNT);
        clearHarmfulEffects(target);
        playHealEffects((ServerLevel) target.level(), target);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResult.CONSUME;
    }

    private static boolean canUseOn(LivingEntity target) {
        if (target.getHealth() < target.getMaxHealth()) {
            return true;
        }
        return hasHarmfulEffects(target);
    }

    private static boolean hasHarmfulEffects(LivingEntity target) {
        for (MobEffectInstance instance : target.getActiveEffects()) {
            if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                return true;
            }
        }
        return false;
    }

    private static void clearHarmfulEffects(LivingEntity target) {
        List<MobEffectInstance> active = new ArrayList<>(target.getActiveEffects());
        for (MobEffectInstance instance : active) {
            if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                target.removeEffect(instance.getEffect());
            }
        }
    }

    /** Particles + allurite hit sound used by shard heals and Heartstone pairing heals. */
    public static void playHealEffects(ServerLevel level, LivingEntity target) {
        double x = target.getX();
        double y = target.getY() + target.getBbHeight() * 0.5D;
        double z = target.getZ();

        level.sendParticles(
                ModParticleTypes.RHODOHEART_RAIN.get(),
                x,
                y,
                z,
                HEAL_PARTICLE_COUNT,
                0.35D,
                0.35D,
                0.35D,
                0.02D
        );

        level.playSound(
                null,
                x,
                y,
                z,
                RhodoheartSoundTypes.alluriteHit(),
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );
    }
}
