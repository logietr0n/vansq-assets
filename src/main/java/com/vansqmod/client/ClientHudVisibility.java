package com.vansqmod.client;

import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModList;

/**
 * Shared HUD visibility for overlays that should respect F1 / Better F1.
 */
public final class ClientHudVisibility {

    private ClientHudVisibility() {
    }

    /**
     * {@code true} when HUD overlays (minimap, instrument text, etc.) should not draw.
     * Honors vanilla {@code hideGui} and Better F1's {@code NO_HUD}/{@code ALL_HIDDEN}
     * (Better F1 temporarily clears {@code hideGui} while drawing hands).
     */
    public static boolean shouldHideHudOverlays() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) {
            return true;
        }
        return isBetterF1HidingHud();
    }

    private static boolean isBetterF1HidingHud() {
        if (!ModList.get().isLoaded("betterf1")) {
            return false;
        }
        try {
            Class<?> betterF1 = Class.forName("com.movtery.betterf1.BetterF1");
            Object state = betterF1.getField("state").get(null);
            if (!(state instanceof Enum<?> hudState)) {
                return false;
            }
            return !"ALL_VISIBLE".equals(hudState.name());
        } catch (ReflectiveOperationException | ClassCastException ignored) {
            return false;
        }
    }
}
