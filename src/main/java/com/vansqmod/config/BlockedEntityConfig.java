package com.vansqmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vansqmod.VansqMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Loads {@code config/vansqmod/blocked_entities.json}. Listed entity IDs are stripped from biome
 * spawn-weight pools, then also blocked on natural spawns, spawners, conversions, chunk load, and
 * {@code addFreshEntity}. Spawn-pool stripping applies at world load; {@code /vansq reloadconfig}
 * only refreshes the join/spawn deny list.
 */
public final class BlockedEntityConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path CONFIG_PATH = Path.of("config/vansqmod/blocked_entities.json");

    private static volatile Set<ResourceLocation> blocked = Set.of();

    private BlockedEntityConfig() {
    }

    public static boolean isBlocked(@Nullable Entity entity) {
        return entity != null && isBlocked(entity.getType());
    }

    public static boolean isBlocked(@Nullable EntityType<?> type) {
        if (type == null || type == EntityType.PLAYER) {
            return false;
        }
        Set<ResourceLocation> ids = blocked;
        if (ids.isEmpty()) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return id != null && ids.contains(id);
    }

    public static int entryCount() {
        return blocked.size();
    }

    public static void load() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                writeDefaultConfig();
            }
            Set<ResourceLocation> parsed = new LinkedHashSet<>();
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray list = root.getAsJsonArray("entities");
                if (list != null) {
                    for (JsonElement element : list) {
                        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                            VansqMod.LOGGER.warn("blocked_entities.json: skipping non-string entry {}", element);
                            continue;
                        }
                        String raw = element.getAsString();
                        ResourceLocation id = ResourceLocation.tryParse(raw);
                        if (id == null) {
                            VansqMod.LOGGER.warn("blocked_entities.json: skipping invalid entity id '{}'", raw);
                            continue;
                        }
                        parsed.add(id);
                    }
                }
            }
            blocked = Set.copyOf(parsed);
            VansqMod.LOGGER.info("Loaded {} blocked entity id(s) from {}", blocked.size(), CONFIG_PATH);
        } catch (Exception e) {
            VansqMod.LOGGER.error("Failed to load {}", CONFIG_PATH, e);
            blocked = Set.of();
        }
    }

    private static void writeDefaultConfig() throws java.io.IOException {
        Files.createDirectories(CONFIG_PATH.getParent());
        JsonObject root = new JsonObject();
        root.addProperty(
                "_comment",
                "Entity IDs that must never appear (natural spawns, spawners, conversions, chunk load, spawn eggs). "
                        + "Example: \"hominid:bellman\". Reload with /vansq reloadconfig."
        );
        JsonArray entities = new JsonArray();
        root.add("entities", entities);
        Files.writeString(CONFIG_PATH, GSON.toJson(root) + System.lineSeparator());
    }
}
