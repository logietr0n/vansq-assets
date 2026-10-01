package com.vansqmod.mixin.yungscavebiomes;

import com.vansqmod.compat.FrostedCavesGelidConversion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * YUNG's {@code SkeletonMixin} sets {@code isInPowderSnow} in Frosted Caves so
 * vanilla freeze-converts to strays. Zombies get the same timer and become Gelids.
 */
@Mixin(Zombie.class)
public abstract class ZombieFrostedCavesGelidMixin extends Monster implements FrostedCavesGelidConversion.Access {

    @Unique
    private static final EntityDataAccessor<Boolean> VANSQMOD$GELID_CONVERTING =
            SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private int vansqmod$inFrostedCavesTime;

    @Unique
    private int vansqmod$gelidConversionTime;

    protected ZombieFrostedCavesGelidMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void vansqmod$gelidConversionData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(VANSQMOD$GELID_CONVERTING, false);
    }

    @Override
    public boolean vansqmod$isGelidConverting() {
        return this.entityData.get(VANSQMOD$GELID_CONVERTING);
    }

    @Unique
    private void vansqmod$setGelidConverting(boolean converting) {
        this.entityData.set(VANSQMOD$GELID_CONVERTING, converting);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void vansqmod$convertZombiesInFrostedCaves(CallbackInfo ci) {
        Zombie zombie = (Zombie) (Object) this;
        if (this.level().isClientSide
                || !this.isAlive()
                || this.isNoAi()
                || !FrostedCavesGelidConversion.isVanillaZombie(zombie)
                || FrostedCavesGelidConversion.gelidType() == null) {
            return;
        }
        if (zombie.isUnderWaterConverting()) {
            this.vansqmod$inFrostedCavesTime = -1;
            this.vansqmod$setGelidConverting(false);
            return;
        }

        if (FrostedCavesGelidConversion.inFrostedCaves(zombie)) {
            if (this.vansqmod$isGelidConverting()) {
                this.vansqmod$gelidConversionTime--;
                if (this.vansqmod$gelidConversionTime < 0) {
                    FrostedCavesGelidConversion.convert(zombie);
                }
            } else {
                this.vansqmod$inFrostedCavesTime++;
                if (this.vansqmod$inFrostedCavesTime >= FrostedCavesGelidConversion.PRE_SHAKE_TICKS) {
                    this.vansqmod$startGelidConversion(FrostedCavesGelidConversion.SHAKE_TICKS);
                }
            }
        } else {
            this.vansqmod$inFrostedCavesTime = -1;
            this.vansqmod$setGelidConverting(false);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void vansqmod$saveGelidConversion(CompoundTag tag, CallbackInfo ci) {
        tag.putInt(
                FrostedCavesGelidConversion.CONVERSION_TIME_TAG,
                this.vansqmod$isGelidConverting() ? this.vansqmod$gelidConversionTime : -1);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void vansqmod$loadGelidConversion(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(FrostedCavesGelidConversion.CONVERSION_TIME_TAG, 99)
                && tag.getInt(FrostedCavesGelidConversion.CONVERSION_TIME_TAG) > -1) {
            this.vansqmod$startGelidConversion(tag.getInt(FrostedCavesGelidConversion.CONVERSION_TIME_TAG));
        }
    }

    @Unique
    private void vansqmod$startGelidConversion(int ticks) {
        this.vansqmod$gelidConversionTime = ticks;
        this.vansqmod$setGelidConverting(true);
    }
}
