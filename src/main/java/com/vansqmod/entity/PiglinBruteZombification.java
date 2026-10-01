package com.vansqmod.entity;

import com.vansqmod.VansqMod;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;

/**
 * Marks zombified piglins that used to be brutes so CEM can keep the brute-shaped
 * {@code zombified_piglin2.jem} after 1.21 dropped {@code HandItems} NBT.
 */
@EventBusSubscriber(modid = VansqMod.MODID)
public final class PiglinBruteZombification {

    public static final String FROM_BRUTE_TAG = "vansqmod_from_brute";

    private PiglinBruteZombification() {
    }

    @SubscribeEvent
    public static void onConverted(LivingConversionEvent.Post event) {
        if (!(event.getEntity() instanceof PiglinBrute) || !(event.getOutcome() instanceof ZombifiedPiglin zombified)) {
            return;
        }
        zombified.getPersistentData().putBoolean(FROM_BRUTE_TAG, true);
        zombified.addTag(FROM_BRUTE_TAG);
    }
}
