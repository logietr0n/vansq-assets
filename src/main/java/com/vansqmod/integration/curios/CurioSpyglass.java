package com.vansqmod.integration.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

public final class CurioSpyglass {

    private CurioSpyglass() {
    }

    public static boolean has(LivingEntity entity) {
        if (entity == null || !ModList.get().isLoaded("curios")) {
            return false;
        }
        try {
            return CuriosApi.getCuriosInventory(entity)
                    .flatMap(inventory -> inventory.findFirstCurio(Items.SPYGLASS))
                    .isPresent();
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
