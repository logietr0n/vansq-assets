package com.vansqmod.mixin.yungscavebiomes;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies YUNG's Cave Biomes {@code buffeted} effect to players on the surface
 * in desert biomes while the weather is thundering — same application pattern
 * as Lost Caves sandstorms (duration 100, reapply under 60, every 10 ticks).
 */
@Mixin(LivingEntity.class)
public abstract class DesertThunderBuffetedMixin {

    @Unique
    private static final int VANSQMOD$BUFFETED_DURATION = 100;

    @Unique
    private static final int VANSQMOD$BUFFETED_REAPPLY_THRESHOLD = 60;

    @Unique
    private static final ResourceLocation VANSQMOD$BUFFETED_ID =
            ResourceLocation.fromNamespaceAndPath("yungscavebiomes", "buffeted");

    @Inject(method = "tick", at = @At("RETURN"))
    private void vansqmod$buffetInDesertThunder(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)
                || self.level().isClientSide()
                || player.isSpectator()
                || self.tickCount % 10 != 0
                || !self.level().isThundering()
                || !self.level().canSeeSky(self.blockPosition())
                || !self.level().getBiome(self.blockPosition()).is(Tags.Biomes.IS_DESERT)) {
            return;
        }

        Holder<MobEffect> buffeted = BuiltInRegistries.MOB_EFFECT.getHolder(VANSQMOD$BUFFETED_ID).orElse(null);
        if (buffeted == null) {
            return;
        }

        MobEffectInstance current = self.getEffect(buffeted);
        if (current == null || current.getDuration() < VANSQMOD$BUFFETED_REAPPLY_THRESHOLD) {
            self.addEffect(new MobEffectInstance(buffeted, VANSQMOD$BUFFETED_DURATION, 0, false, false, true));
        }
    }
}
