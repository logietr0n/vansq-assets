package com.vansqmod.mixin.spideroverhaul;

import dev.chybx.spideroverhaul.entity.desert_spider.CactusSpineEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Desert Spider death/hurt spines fly at -15° to 75° from horizontal at speed 0.5.
 * Thorns-on-hit is replaced by a 6–9 spine burst whenever they actually take damage.
 * Death bursts 15–20 spines instead of the original 10.
 */
@Mixin(targets = "dev.chybx.spideroverhaul.entity.DesertSpiderEntity", remap = false)
public abstract class DesertSpiderSpikeBurstMixin {

    @Unique
    private static final float VANSQMOD$SPINE_VELOCITY = 0.5F;

    @Unique
    private static final double VANSQMOD$MIN_PITCH_DEG = -15.0;

    @Unique
    private static final double VANSQMOD$MAX_PITCH_DEG = 75.0;

    @Unique
    private static final int VANSQMOD$HURT_SPINES_MIN = 6;

    @Unique
    private static final int VANSQMOD$HURT_SPINES_MAX = 9;

    @Unique
    private static final int VANSQMOD$DEATH_SPINES_MIN = 15;

    @Unique
    private static final int VANSQMOD$DEATH_SPINES_MAX = 20;

    @Inject(method = "fireSpikeBurst", at = @At("HEAD"), cancellable = true)
    private void vansqmod$deathSpineCount(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level().isClientSide) {
            vansqmod$fireSpines(VANSQMOD$DEATH_SPINES_MIN, VANSQMOD$DEATH_SPINES_MAX);
        }
        ci.cancel();
    }

    @Redirect(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    remap = true
            )
    )
    private boolean vansqmod$skipThornsDamage(Entity attacker, DamageSource source, float amount) {
        return false;
    }

    @Redirect(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V",
                    remap = true
            )
    )
    private void vansqmod$skipThornsSound(Entity attacker, SoundEvent sound, float volume, float pitch) {
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void vansqmod$spinesOnDamaged(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!cir.getReturnValueZ() || self.level().isClientSide || !self.isAlive()) {
            return;
        }
        vansqmod$fireSpines(VANSQMOD$HURT_SPINES_MIN, VANSQMOD$HURT_SPINES_MAX);
    }

    @Unique
    private void vansqmod$fireSpines(int min, int max) {
        LivingEntity self = (LivingEntity) (Object) this;
        Level level = self.level();
        RandomSource random = self.getRandom();
        int count = Mth.randomBetweenInclusive(random, min, max);
        for (int i = 0; i < count; i++) {
            CactusSpineEntity spine = new CactusSpineEntity(level, self);
            vansqmod$launchSpine(spine, random);
            level.addFreshEntity(spine);
        }
        self.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
    }

    @Unique
    private void vansqmod$launchSpine(CactusSpineEntity spine, RandomSource random) {
        double yaw = random.nextDouble() * (Math.PI * 2.0);
        double pitchDeg = Mth.lerp(random.nextDouble(), VANSQMOD$MIN_PITCH_DEG, VANSQMOD$MAX_PITCH_DEG);
        double pitch = Math.toRadians(pitchDeg);
        double cosPitch = Math.cos(pitch);
        double dirX = Math.cos(yaw) * cosPitch;
        double dirY = Math.sin(pitch);
        double dirZ = Math.sin(yaw) * cosPitch;
        spine.setPos(spine.getX() + dirX * 0.4, spine.getY(), spine.getZ() + dirZ * 0.4);
        spine.shoot(dirX, dirY, dirZ, VANSQMOD$SPINE_VELOCITY, 0.0F);
    }
}
