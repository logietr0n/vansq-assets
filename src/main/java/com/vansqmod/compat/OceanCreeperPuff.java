package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import tech.thatgravyboat.creeperoverhaul.common.entity.custom.PufferfishCreeper;
import tech.thatgravyboat.creeperoverhaul.common.registry.ModSounds;

/**
 * Ocean Creepers ({@code creeperoverhaul:ocean_creeper}) have three puff stages
 * (deflated, half, full). Each inflate step takes 24 ticks (1.2s). A hit drops
 * one stage and does nothing at stage 0. Sting is 2/4/6 (4/8/12 charged) with
 * Poison II for 6s at half and 12s at full.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class OceanCreeperPuff {

    /** 1.2 seconds at 20 tps. */
    public static final int STAGE_DELAY_TICKS = 24;
    public static final int MAX_PUFF_STATE = 2;
    public static final int POISON_AMPLIFIER = 1;
    /** Original Creeper Overhaul is 60 ticks per stage. */
    public static final int POISON_TICKS_PER_STAGE = 120;

    private OceanCreeperPuff() {
    }

    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent.Post event) {
        if (!ModList.get().isLoaded("creeperoverhaul")) {
            return;
        }
        if (!(event.getEntity() instanceof PufferfishCreeper creeper)) {
            return;
        }
        if (creeper.level().isClientSide || event.getNewDamage() <= 0.0F) {
            return;
        }
        dropOneStage(creeper);
    }

    public static void dropOneStage(PufferfishCreeper creeper) {
        byte state = creeper.getPuffState();
        if (state <= 0) {
            return;
        }
        PufferfishCreeperAccess access = (PufferfishCreeperAccess) creeper;
        creeper.playSound(ModSounds.OCEAN_DEFLATE.get());
        creeper.setPuffState(state - 1);
        creeper.refreshDimensions();
        if (access.vansqmod$getInflateCounter() > 0) {
            access.vansqmod$setInflateCounter(1);
        }
        access.vansqmod$setDeflateTimer(0);
    }

    public static boolean sting(PufferfishCreeper creeper, Entity entity) {
        byte state = creeper.getPuffState();
        float damage = stingDamage(state, creeper.isPowered());
        if (!entity.hurt(creeper.level().damageSources().mobAttack(creeper), damage)) {
            return false;
        }
        creeper.playSound(SoundEvents.PUFFER_FISH_STING, 1.0F, 1.0F);
        if (state > 0 && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(
                    MobEffects.POISON,
                    POISON_TICKS_PER_STAGE * state,
                    POISON_AMPLIFIER));
        }
        return true;
    }

    public static float stingDamage(int state, boolean charged) {
        float damage = 2.0F * (state + 1);
        return charged ? damage * 2.0F : damage;
    }

    public static void tickPuff(PufferfishCreeper creeper, PufferfishCreeperAccess access) {
        int inflateCounter = access.vansqmod$getInflateCounter();
        int deflateTimer = access.vansqmod$getDeflateTimer();
        byte state = creeper.getPuffState();
        if (inflateCounter > 0) {
            if (state < MAX_PUFF_STATE && inflateCounter > STAGE_DELAY_TICKS) {
                creeper.playSound(ModSounds.OCEAN_INFLATE.get());
                creeper.setPuffState(state + 1);
                access.vansqmod$setInflateCounter(1);
            } else {
                access.vansqmod$setInflateCounter(inflateCounter + 1);
            }
        } else if (state != 0) {
            if (deflateTimer > 60 && state == 2) {
                creeper.playSound(ModSounds.OCEAN_DEFLATE.get());
                creeper.setPuffState(1);
            } else if (deflateTimer > 100 && state == 1) {
                creeper.playSound(ModSounds.OCEAN_DEFLATE.get());
                creeper.setPuffState(0);
            }
            access.vansqmod$setDeflateTimer(deflateTimer + 1);
        }
    }

    public interface PufferfishCreeperAccess {
        int vansqmod$getInflateCounter();

        void vansqmod$setInflateCounter(int value);

        int vansqmod$getDeflateTimer();

        void vansqmod$setDeflateTimer(int value);
    }
}
