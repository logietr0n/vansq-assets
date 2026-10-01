package com.vansqmod.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Vanilla phantom swoop plays at volume 10. Minecraft treats volume above 1 as
 * extra hear distance ({@code 16 * volume} blocks), so the screech carries 160
 * blocks and barely fades. Cap at 3 so dives starting 20–40 blocks overhead
 * still warn, while far-off phantoms stay quiet.
 */
@Mixin(Phantom.class)
public abstract class PhantomSoundRangeMixin extends FlyingMob {

    private static final float MAX_SOUND_VOLUME = 3.0F;

    protected PhantomSoundRangeMixin(EntityType<? extends FlyingMob> type, Level level) {
        super(type, level);
    }

    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        super.playSound(sound, Math.min(volume, MAX_SOUND_VOLUME), pitch);
    }
}
