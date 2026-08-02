package com.vansqmod.config;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/** Where custom tooltip lines are inserted relative to the rest of the tooltip. */
public enum TooltipPlacement {
    /** Directly under the item name (default). */
    AFTER_NAME,
    /** At the end of the tooltip, after enchantments and attributes. */
    BOTTOM;

    public static TooltipPlacement parse(@Nullable String value, TooltipPlacement fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT).replace('-', '_')) {
            case "bottom", "end", "tail" -> BOTTOM;
            case "after_name", "aftername", "top", "name", "under_name" -> AFTER_NAME;
            default -> fallback;
        };
    }
}
