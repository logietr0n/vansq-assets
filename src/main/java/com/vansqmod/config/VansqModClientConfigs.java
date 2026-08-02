package com.vansqmod.config;

/** Loads all client-side JSON configs under {@code config/vansqmod/}. */
public final class VansqModClientConfigs {

    private VansqModClientConfigs() {
    }

    public static void loadAll() {
        CustomTooltipConfig.load();
        ItemNameColorConfig.load();
    }
}
