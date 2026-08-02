package com.vansqmod.config;

import net.minecraft.network.chat.Component;

import java.util.List;

public record CustomTooltipEntry(List<Component> lines, TooltipPlacement placement) {

    public CustomTooltipEntry {
        lines = List.copyOf(lines);
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }
}
