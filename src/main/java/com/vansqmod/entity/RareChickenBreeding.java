package com.vansqmod.entity;

import com.vansqmod.VansqMod;
import net.minecraft.world.entity.animal.Chicken;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;

@EventBusSubscriber(modid = VansqMod.MODID)
public final class RareChickenBreeding {

    private RareChickenBreeding() {
    }

    @SubscribeEvent
    public static void onBabySpawn(BabyEntitySpawnEvent event) {
        if (!ModList.get().isLoaded("vanillabackport")) {
            return;
        }
        if (!(event.getChild() instanceof Chicken child)
                || !(event.getParentA() instanceof Chicken parentA)
                || !(event.getParentB() instanceof Chicken parentB)) {
            return;
        }
        RareChickenVariants.applyBreeding(child, parentA, parentB);
    }
}
