package com.vansqmod.mixin.creeperoverhaul;

import com.vansqmod.compat.OceanCreeperPuff;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.thatgravyboat.creeperoverhaul.common.entity.base.CreeperType;
import tech.thatgravyboat.creeperoverhaul.common.entity.base.WaterCreeper;
import tech.thatgravyboat.creeperoverhaul.common.entity.custom.PufferfishCreeper;

@Mixin(targets = "tech.thatgravyboat.creeperoverhaul.common.entity.custom.PufferfishCreeper")
public abstract class OceanCreeperPuffMixin extends WaterCreeper implements OceanCreeperPuff.PufferfishCreeperAccess {

    @Shadow(remap = false)
    private int inflateCounter;

    @Shadow(remap = false)
    private int deflateTimer;

    protected OceanCreeperPuffMixin(EntityType<? extends Creeper> entityType, Level level, CreeperType type) {
        super(entityType, level, type);
    }

    @Override
    public int vansqmod$getInflateCounter() {
        return this.inflateCounter;
    }

    @Override
    public void vansqmod$setInflateCounter(int value) {
        this.inflateCounter = value;
    }

    @Override
    public int vansqmod$getDeflateTimer() {
        return this.deflateTimer;
    }

    @Override
    public void vansqmod$setDeflateTimer(int value) {
        this.deflateTimer = value;
    }

    @Inject(method = "doHurtTarget", at = @At("HEAD"), cancellable = true)
    private void vansqmod$stingDamage(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(OceanCreeperPuff.sting((PufferfishCreeper) (Object) this, entity));
    }

    /**
     * @author vansqmod
     * @reason Stage-by-stage inflate. Creeper Overhaul copies vanilla
     * pufferfish and jumps 0→1 in the same tick.
     */
    @Overwrite
    public void tick() {
        PufferfishCreeper creeper = (PufferfishCreeper) (Object) this;
        if (!this.level().isClientSide && this.isAlive() && this.isEffectiveAi()) {
            OceanCreeperPuff.tickPuff(creeper, this);
        }
        super.tick();
    }
}
