package com.vansqmod.client;

import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.interfaces.config.IDhApiConfigValue;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;

/**
 * Temporary Distant Horizons {@code enableVanillaFog} API override (does not write TOML).
 * Only touches the API on needed/active transitions to avoid fog flicker from repeated setValue.
 */
@OnlyIn(Dist.CLIENT)
public final class DhVanillaFogOverride {

    private static boolean needed;
    private static boolean overrideActive;

    private DhVanillaFogOverride() {
    }

    public static void setNeeded(boolean value) {
        if (needed == value) {
            // Retry if desired state and applied state have diverged (late DH init / failed clear).
            if (needed != overrideActive) {
                apply();
            }
            return;
        }
        needed = value;
        apply();
    }

    /** Retry apply while desired and applied state disagree. */
    public static void tick() {
        if (needed != overrideActive) {
            apply();
        }
    }

    private static void apply() {
        if (!ModList.get().isLoaded("distanthorizons")) {
            overrideActive = false;
            return;
        }
        try {
            if (DhApi.Delayed.configs == null) {
                return;
            }
            IDhApiConfigValue<Boolean> vanillaFog =
                    DhApi.Delayed.configs.graphics().fog().enableVanillaFog();
            if (vanillaFog == null || !vanillaFog.getCanBeOverrodeByApi()) {
                return;
            }

            if (needed) {
                if (!overrideActive && vanillaFog.setValue(Boolean.TRUE)) {
                    overrideActive = true;
                }
            } else if (overrideActive) {
                if (vanillaFog.clearValue()) {
                    overrideActive = false;
                }
            }
        } catch (Throwable ignored) {
            // DH not fully initialized or API shape changed — leave fog alone.
        }
    }
}
