package com.vansqmod.item;

import com.vansqmod.VansqMod;
import com.vansqmod.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;

public final class CrownCuriosIntegration {

    private static final CrownCurioItem CROWN = new CrownCurioItem();

    private CrownCuriosIntegration() {
    }

    public static void init(IEventBus modBus) {
        if (!ModList.get().isLoaded("curios")) {
            return;
        }
        modBus.addListener(CrownCuriosIntegration::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CuriosApi.registerCurio(ModItems.GOLDEN_CROWN.get(), CROWN);
            CuriosApi.registerCurio(ModItems.SILVER_CROWN.get(), CROWN);
        });
    }
}
