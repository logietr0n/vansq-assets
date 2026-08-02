package com.vansqmod.integration.dungeonsdelight;

import com.vansqmod.VansqMod;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = VansqMod.MODID)
public final class LivingFireCrypticEyeIntegration {

    private LivingFireCrypticEyeIntegration() {
    }

    @SubscribeEvent
    public static void onItemEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }
        LivingFireCrypticEyeHandler.tryTransformEnderEye(itemEntity);
    }
}
