package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = VansqMod.MODID)
public final class GoatManDifficultyEvents {

    private GoatManDifficultyEvents() {
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!GoatManDifficulty.isGoatMan(attacker) || !GoatManDifficulty.isHardcoreLenient(attacker.level())) {
            return;
        }
        event.setAmount(event.getAmount() * 0.5F);
    }
}
