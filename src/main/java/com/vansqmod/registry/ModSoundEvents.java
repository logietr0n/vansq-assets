package com.vansqmod.registry;

import com.vansqmod.VansqMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSoundEvents {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, VansqMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> MELLOWED_AMBIENT =
            register("entity.mellowed.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MELLOWED_HURT =
            register("entity.mellowed.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MELLOWED_DEATH =
            register("entity.mellowed.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> MELLOWED_STEP =
            register("entity.mellowed.step");

    public static final DeferredHolder<SoundEvent, SoundEvent> BOULDERING_ZOMBIE_AMBIENT =
            register("entity.bouldering_zombie.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOULDERING_ZOMBIE_HURT =
            register("entity.bouldering_zombie.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOULDERING_ZOMBIE_DEATH =
            register("entity.bouldering_zombie.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOULDERING_ZOMBIE_STEP =
            register("entity.bouldering_zombie.step");

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_BOSS_WITHER_INTRO =
            register("music.boss.wither.intro");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_BOSS_WITHER_LOOP =
            register("music.boss.wither.loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_BOSS_MISSIONARY_INTRO =
            register("music.boss.missionary.intro");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_BOSS_MISSIONARY_LOOP =
            register("music.boss.missionary.loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_BOSS_ENDERMAN_INTRO =
            register("music.boss.enderman.intro");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_BOSS_ENDERMAN_LOOP =
            register("music.boss.enderman.loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_BOSS_BERSERKER_INTRO =
            register("music.boss.berserker.intro");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_BOSS_BERSERKER_LOOP =
            register("music.boss.berserker.loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_BOSS_VOID_WORM_LOOP =
            register("music.boss.void_worm.loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_BOSS_VOID_WORM_OUTRO =
            register("music.boss.void_worm.outro");

    public static final DeferredHolder<SoundEvent, SoundEvent> SWEEP =
            register("item.knife_sweeping.sweep");

    private ModSoundEvents() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, name)));
    }
}
