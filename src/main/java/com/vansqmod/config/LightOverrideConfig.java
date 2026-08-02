package com.vansqmod.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class LightOverrideConfig {

    private static final Gson GSON = new Gson();
    private static final Path CONFIG_PATH = Path.of("config/vansqmod/light_overrides.json");

    public static final Map<ResourceLocation, Integer> LIGHT_MAP = new HashMap<>();

    public static void load() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                Files.createDirectories(CONFIG_PATH.getParent());

                // default file
                Map<String, Integer> defaults = Map.of(
                        "yungscavebiomes:icicle", 10
                );

                Files.writeString(CONFIG_PATH, GSON.toJson(defaults));
            }

            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                Map<String, Integer> raw = GSON.fromJson(reader, new TypeToken<Map<String, Integer>>(){}.getType());

                LIGHT_MAP.clear();

                for (Map.Entry<String, Integer> entry : raw.entrySet()) {
                    LIGHT_MAP.put(ResourceLocation.parse(entry.getKey()), entry.getValue());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}