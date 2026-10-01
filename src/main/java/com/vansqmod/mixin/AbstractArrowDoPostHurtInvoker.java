package com.vansqmod.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractArrow.class)
public interface AbstractArrowDoPostHurtInvoker {

    @Invoker("doPostHurtEffects")
    void vansqmod$invokeDoPostHurtEffects(LivingEntity target);
}
