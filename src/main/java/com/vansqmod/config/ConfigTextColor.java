package com.vansqmod.config;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/** Parses named Minecraft colors and hex strings for config files. */
public final class ConfigTextColor {

    private ConfigTextColor() {
    }

    public static @Nullable TextColor resolve(@Nullable String colorName) {
        if (colorName == null || colorName.isBlank()) {
            return null;
        }
        String trimmed = colorName.trim();
        if (trimmed.startsWith("#")) {
            return parseHex(trimmed);
        }
        ChatFormatting fmt = namedFormatting(trimmed);
        if (fmt != null) {
            return TextColor.fromLegacyFormat(fmt);
        }
        return null;
    }

    public static @Nullable ChatFormatting namedFormatting(String colorName) {
        String normalized = colorName.toLowerCase(Locale.ROOT).replace(' ', '_');
        return switch (normalized) {
            case "light_gray", "lightgray", "grey", "light_grey", "lightgrey" -> ChatFormatting.GRAY;
            case "dark_grey", "darkgrey" -> ChatFormatting.DARK_GRAY;
            default -> {
                for (ChatFormatting fmt : ChatFormatting.values()) {
                    if (fmt.isColor() && fmt.getName().equals(normalized)) {
                        yield fmt;
                    }
                }
                yield null;
            }
        };
    }

    public static @Nullable TextColor parseHex(String hex) {
        if (!hex.startsWith("#")) {
            return null;
        }
        String digits = hex.substring(1);
        try {
            int rgb = switch (digits.length()) {
                case 3 -> {
                    int r = Integer.parseInt(digits.substring(0, 1), 16);
                    int g = Integer.parseInt(digits.substring(1, 2), 16);
                    int b = Integer.parseInt(digits.substring(2, 3), 16);
                    yield (r << 20) | (r << 16) | (g << 12) | (g << 8) | (b << 4) | b;
                }
                case 6 -> Integer.parseInt(digits, 16);
                default -> -1;
            };
            if (rgb < 0) {
                return null;
            }
            return TextColor.fromRgb(rgb);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
