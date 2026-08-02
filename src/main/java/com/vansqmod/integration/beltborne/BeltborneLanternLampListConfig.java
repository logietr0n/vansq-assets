package com.vansqmod.integration.beltborne;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Reads Beltborne's simple lamp list config file at {@code config/beltborne_lanterns_lamps.json}.
 *
 * <p>This is intentionally separate from Beltborne's own config system; some packs edit this file directly.</p>
 */
/**
 * Deprecated: we now hardcode lamp compatibility in code (pack request).
 * Kept to avoid breaking references during transition; safe to delete later.
 */
@Deprecated
final class BeltborneLanternLampListConfig {

    private static final String FILE_NAME = "beltborne_lanterns_lamps.json";
    private static final Gson GSON = new Gson();

    private BeltborneLanternLampListConfig() {
    }

    static List<ResourceLocation> readLampIds() {
        Path path = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
        if (!Files.exists(path)) {
            return List.of();
        }

        try {
            String raw = Files.readString(path, StandardCharsets.UTF_8);
            JsonElement rootEl = JsonParser.parseString(raw);
            if (!(rootEl instanceof JsonObject root)) {
                return List.of();
            }
            JsonElement arrEl = root.get("extraLampLight");
            if (!(arrEl instanceof JsonArray arr)) {
                return List.of();
            }

            Set<ResourceLocation> out = new LinkedHashSet<>();
            for (JsonElement el : arr) {
                if (el == null || !el.isJsonPrimitive()) continue;
                String s = el.getAsString();
                ResourceLocation id = ResourceLocation.tryParse(s);
                if (id != null) out.add(id);
            }
            return Collections.unmodifiableList(new ArrayList<>(out));
        } catch (Throwable ignored) {
            return List.of();
        }
    }
}

