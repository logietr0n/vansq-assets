package com.vansqmod.integration.beltborne;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.oxcodsnet.beltborne_lanterns.common.compat.CompatibilityLayer;

import java.util.Optional;

/**
 * Beltborne {@link CompatibilityLayer} SPI: Curios {@code belt} is the source of truth for equipped lamps.
 */
public final class BeltborneCuriosCompatibilityLayer implements CompatibilityLayer {

    public static final BeltborneCuriosCompatibilityLayer INSTANCE = new BeltborneCuriosCompatibilityLayer();

    /** Public for {@link java.util.ServiceLoader} and {@link #INSTANCE}. */
    public BeltborneCuriosCompatibilityLayer() {
    }

    @Override
    public String getModId() {
        return "curios";
    }

    /**
     * Consumes the B-key toggle packet so virtual belt state is not toggled from the hand.
     */
    @Override
    public boolean tryToggleLantern(ServerPlayer player) {
        return true;
    }

    @Override
    public Optional<ItemStack> getBeltStack(ServerPlayer player) {
        return BeltborneLanternEquipment.getBeltLamp(player);
    }

    @Override
    public boolean handlesItemOnDeath() {
        return true;
    }
}
