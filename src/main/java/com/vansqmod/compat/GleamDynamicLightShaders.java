package com.vansqmod.compat;

/**
 * Gleam crushes every 16-block cell to the brightest source, which hides extra
 * colors in the same cell. Strip only that crush and keep Gleam's cheap per-cell walk.
 */
public final class GleamDynamicLightShaders {

    private static final String MARKER = "vansqNoCellCrush";
    private static final String VANILLA_CRUSH = String.join("\n",
            "    float totalColoredLum = dot(coloredLightSum, LUM_WEIGHTS);",
            "    float maxSingleColoredLum = max(maxOccludedLum * occlusionFactor, maxNonOccludedLum);",
            "    float maxAllowed = max(maxSingleColoredLum, MIN_VISIBLE_LUM);",
            "    if (maxSingleColoredLum > 0.0 && totalColoredLum > maxAllowed) coloredLightSum *= maxAllowed / totalColoredLum;"
    );
    private static final String SODIUM_CRUSH = String.join("\n",
            "    float totalColoredLum = dot(coloredLightSum, LUM_WEIGHTS);",
            "    float maxSingleColoredLum = max(maxOccludedLum * blockFactor, maxNonOccludedLum);",
            "    float maxAllowed = max(maxSingleColoredLum, MIN_VISIBLE_LUM);",
            "    if (maxSingleColoredLum > 0.0 && totalColoredLum > maxAllowed) coloredLightSum *= maxAllowed / totalColoredLum;"
    );
    private static final String NO_CRUSH = "    // " + MARKER;

    private GleamDynamicLightShaders() {
    }

    public static String fix(String source) {
        if (source == null || source.isEmpty() || source.contains(MARKER)) {
            return source;
        }
        if (source.contains(VANILLA_CRUSH)) {
            return source.replace(VANILLA_CRUSH, NO_CRUSH);
        }
        if (source.contains(SODIUM_CRUSH)) {
            return source.replace(SODIUM_CRUSH, NO_CRUSH);
        }
        return source.replace(
                "if (maxSingleColoredLum > 0.0 && totalColoredLum > maxAllowed) coloredLightSum *= maxAllowed / totalColoredLum;",
                NO_CRUSH
        );
    }
}
