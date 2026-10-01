package com.vansqmod.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vansqmod.VansqMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.animal.Chicken;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Loads the Blockbench rare-chicken model and bakes it as a vanilla
 * {@link ChickenModel}, so walk/flap/look animations stay identical.
 * Edit {@code assets/vansqmod/models/entity/rare_chicken.bbmodel} (adult) and
 * {@code rare_chicken_baby.bbmodel} (Tiny Takeover baby) in Blockbench.
 * Keep the bone names {@code head}, {@code beak}, {@code red_thing},
 * {@code body}, {@code right_leg}, {@code left_leg}, {@code right_wing},
 * {@code left_wing} or those animations will not bind.
 */
public enum RareChickenModels implements ResourceManagerReloadListener {
    INSTANCE;

    public static final ResourceLocation MODEL_PATH =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "models/entity/rare_chicken.bbmodel");
    public static final ResourceLocation BABY_MODEL_PATH =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "models/entity/rare_chicken_baby.bbmodel");
    public static final ResourceLocation BABY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "textures/entity/chicken/rare_chicken_baby.png");
    private static final ResourceLocation LEGACY_MODEL_PATH =
            ResourceLocation.fromNamespaceAndPath(VansqMod.MODID, "models/entity/rare_chicken.json");
    private static final Set<String> CHICKEN_PARTS = Set.of(
            "head", "beak", "red_thing", "body", "right_leg", "left_leg", "right_wing", "left_wing"
    );
    /**
     * Blockbench (Y-up) stores chickens standing on {@code y=0}. Java entity
     * models use Y-down with the same 24-unit height, so pivots convert with
     * {@code javaY = 24 - blockbenchY}.
     */
    private static final float BLOCKBENCH_GROUND = 24.0F;

    private ChickenModel<Chicken> model;
    private ChickenModel<Chicken> babyModel;

    public ChickenModel<Chicken> get() {
        if (model == null) {
            bakeAll(Minecraft.getInstance().getResourceManager());
        }
        return model;
    }

    public ChickenModel<Chicken> getBaby() {
        if (babyModel == null) {
            bakeAll(Minecraft.getInstance().getResourceManager());
        }
        return babyModel;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        bakeAll(resourceManager);
    }

    private void bakeAll(ResourceManager resourceManager) {
        model = bake(resourceManager, MODEL_PATH, LEGACY_MODEL_PATH, ChickenModel::createBodyLayer);
        babyModel = bake(resourceManager, BABY_MODEL_PATH, null, RareChickenModels::createBabyBodyLayer);
    }

    private static ChickenModel<Chicken> bake(
            ResourceManager resourceManager,
            ResourceLocation path,
            ResourceLocation fallbackPath,
            java.util.function.Supplier<LayerDefinition> vanilla
    ) {
        Optional<LayerDefinition> loaded = tryLoad(resourceManager, path);
        if (loaded.isEmpty() && fallbackPath != null) {
            loaded = tryLoad(resourceManager, fallbackPath);
        }
        LayerDefinition definition = loaded.orElseGet(vanilla);
        return new ChickenModel<>(definition.bakeRoot());
    }

    private static Optional<LayerDefinition> tryLoad(ResourceManager resourceManager, ResourceLocation path) {
        try {
            Optional<Resource> resource = resourceManager.getResource(path);
            if (resource.isEmpty()) {
                return Optional.empty();
            }
            try (BufferedReader reader = resource.get().openAsReader()) {
                return Optional.of(parseLayer(JsonParser.parseReader(reader).getAsJsonObject()));
            }
        } catch (Exception e) {
            VansqMod.LOGGER.warn("Failed to load rare chicken model {}, using vanilla chicken", path, e);
            return Optional.empty();
        }
    }

    private static LayerDefinition parseLayer(JsonObject json) {
        if (json.has("minecraft:geometry")) {
            return parseBedrock(json);
        }
        if (json.has("outliner") && json.has("elements")) {
            return parseBlockbench(json);
        }
        return parseLegacyParts(json);
    }

    private static LayerDefinition parseBlockbench(JsonObject json) {
        JsonObject resolution = json.has("resolution") ? json.getAsJsonObject("resolution") : new JsonObject();
        int textureWidth = resolution.has("width") ? resolution.get("width").getAsInt() : 64;
        int textureHeight = resolution.has("height") ? resolution.get("height").getAsInt() : 32;
        Map<String, JsonObject> elements = new HashMap<>();
        for (JsonElement element : json.getAsJsonArray("elements")) {
            JsonObject cube = element.getAsJsonObject();
            if (cube.has("uuid")) {
                elements.put(cube.get("uuid").getAsString(), cube);
            }
        }
        Map<String, JsonObject> groups = new HashMap<>();
        if (json.has("groups")) {
            for (JsonElement element : json.getAsJsonArray("groups")) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject group = element.getAsJsonObject();
                if (group.has("uuid")) {
                    groups.put(group.get("uuid").getAsString(), group);
                }
            }
        }
        Map<String, PartBuild> parts = new HashMap<>();
        for (JsonElement node : json.getAsJsonArray("outliner")) {
            collectBlockbench(node, elements, groups, parts, null, null);
        }
        if (parts.values().stream().allMatch(part -> part.cubes.isEmpty())) {
            throw new IllegalStateException("Blockbench chicken had no named cubes");
        }
        return bakeParts(parts, textureWidth, textureHeight);
    }

    private static void collectBlockbench(
            JsonElement node,
            Map<String, JsonObject> elements,
            Map<String, JsonObject> groups,
            Map<String, PartBuild> parts,
            PartBuild current,
            float[] pivot
    ) {
        if (node.isJsonPrimitive()) {
            addBlockbenchCube(elements.get(node.getAsString()), parts, current);
            return;
        }
        if (!node.isJsonObject()) {
            return;
        }
        JsonObject outline = node.getAsJsonObject();
        JsonObject group = outline;
        if (outline.has("uuid")) {
            JsonObject named = groups.get(outline.get("uuid").getAsString());
            if (named != null) {
                group = named;
            }
        }
        float[] groupPivot = group.has("origin")
                ? javaPivotFromBlockbench(floats(group.getAsJsonArray("origin"), 3))
                : pivot;
        PartBuild next = current;
        String partName = chickenPartName(group, elements);
        if (partName != null) {
            next = parts.computeIfAbsent(partName, ignored -> new PartBuild(partName));
            if (groupPivot != null) {
                next.pivot = groupPivot;
            }
            if (group.has("rotation")) {
                next.rotation = javaRotationFromBlockbench(floats(group.getAsJsonArray("rotation"), 3));
            }
        }
        if (outline.has("uuid")) {
            addBlockbenchCube(elements.get(outline.get("uuid").getAsString()), parts, next);
        }
        if (!outline.has("children")) {
            return;
        }
        for (JsonElement child : outline.getAsJsonArray("children")) {
            collectBlockbench(child, elements, groups, parts, next, groupPivot);
        }
    }

    private static String chickenPartName(JsonObject group, Map<String, JsonObject> elements) {
        if (group.has("name")) {
            String name = group.get("name").getAsString();
            if (CHICKEN_PARTS.contains(name)) {
                return name;
            }
        }
        if (group.has("uuid")) {
            JsonObject cube = elements.get(group.get("uuid").getAsString());
            if (cube != null && cube.has("name")) {
                String name = cube.get("name").getAsString();
                if (CHICKEN_PARTS.contains(name)) {
                    return name;
                }
            }
        }
        return null;
    }

    private static void addBlockbenchCube(JsonObject cube, Map<String, PartBuild> parts, PartBuild current) {
        if (cube == null) {
            return;
        }
        PartBuild target = current;
        if (target == null && cube.has("name")) {
            String name = cube.get("name").getAsString();
            if (CHICKEN_PARTS.contains(name)) {
                target = parts.computeIfAbsent(name, ignored -> new PartBuild(name));
            }
        }
        if (target != null) {
            target.cubes.add(CubeBuild.fromBlockbench(cube, target.pivot));
        }
    }

    private static LayerDefinition parseBedrock(JsonObject json) {
        JsonArray geometries = json.getAsJsonArray("minecraft:geometry");
        JsonObject geometry = geometries.get(0).getAsJsonObject();
        JsonObject description = geometry.getAsJsonObject("description");
        int textureWidth = description.has("texture_width") ? description.get("texture_width").getAsInt() : 64;
        int textureHeight = description.has("texture_height") ? description.get("texture_height").getAsInt() : 32;
        Map<String, PartBuild> parts = new HashMap<>();
        for (JsonElement boneElement : geometry.getAsJsonArray("bones")) {
            JsonObject bone = boneElement.getAsJsonObject();
            String name = bone.get("name").getAsString();
            if (!CHICKEN_PARTS.contains(name)) {
                continue;
            }
            PartBuild part = parts.computeIfAbsent(name, ignored -> new PartBuild(name));
            if (bone.has("pivot")) {
                part.pivot = javaPivotFromBlockbench(floats(bone.getAsJsonArray("pivot"), 3));
            }
            if (bone.has("rotation")) {
                part.rotation = javaRotationFromBlockbench(floats(bone.getAsJsonArray("rotation"), 3));
            }
            if (!bone.has("cubes")) {
                continue;
            }
            for (JsonElement cubeElement : bone.getAsJsonArray("cubes")) {
                part.cubes.add(CubeBuild.fromBedrock(cubeElement.getAsJsonObject(), part.pivot));
            }
        }
        return bakeParts(parts, textureWidth, textureHeight);
    }

    private static LayerDefinition parseLegacyParts(JsonObject json) {
        int textureWidth = json.has("texture_width") ? json.get("texture_width").getAsInt() : 64;
        int textureHeight = json.has("texture_height") ? json.get("texture_height").getAsInt() : 32;
        Map<String, PartBuild> parts = new HashMap<>();
        for (JsonElement element : json.getAsJsonArray("parts")) {
            JsonObject partJson = element.getAsJsonObject();
            String name = partJson.get("name").getAsString();
            PartBuild part = parts.computeIfAbsent(name, ignored -> new PartBuild(name));
            part.pivot = floats(partJson.getAsJsonArray("pivot"), 3);
            if (partJson.has("rotation")) {
                part.rotation = floats(partJson.getAsJsonArray("rotation"), 3);
            }
            for (JsonElement boxElement : partJson.getAsJsonArray("boxes")) {
                JsonObject box = boxElement.getAsJsonObject();
                CubeBuild cube = new CubeBuild();
                cube.uv = floats(box.getAsJsonArray("uv"), 2);
                cube.from = floats(box.getAsJsonArray("from"), 3);
                cube.size = floats(box.getAsJsonArray("size"), 3);
                cube.inflate = box.has("inflate") ? box.get("inflate").getAsFloat() : 0.0F;
                cube.mirror = box.has("mirror") && box.get("mirror").getAsBoolean();
                part.cubes.add(cube);
            }
        }
        return bakeParts(parts, textureWidth, textureHeight);
    }

    private static LayerDefinition bakeParts(Map<String, PartBuild> parts, int textureWidth, int textureHeight) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        for (String name : CHICKEN_PARTS) {
            PartBuild part = parts.get(name);
            if (part == null) {
                root.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
                continue;
            }
            CubeListBuilder cubes = CubeListBuilder.create();
            for (CubeBuild cube : part.cubes) {
                CubeListBuilder builder = cubes.texOffs(Math.round(cube.uv[0]), Math.round(cube.uv[1]));
                if (cube.mirror) {
                    builder.mirror();
                }
                builder.addBox(
                        cube.from[0],
                        cube.from[1],
                        cube.from[2],
                        cube.size[0],
                        cube.size[1],
                        cube.size[2],
                        new CubeDeformation(cube.inflate)
                );
            }
            float rotX = 0.0F;
            float rotY = 0.0F;
            float rotZ = 0.0F;
            if (part.rotation != null) {
                rotX = (float) Math.toRadians(part.rotation[0]);
                rotY = (float) Math.toRadians(part.rotation[1]);
                rotZ = (float) Math.toRadians(part.rotation[2]);
            }
            root.addOrReplaceChild(
                    name,
                    cubes,
                    PartPose.offsetAndRotation(
                            part.pivot[0],
                            part.pivot[1],
                            part.pivot[2],
                            rotX,
                            rotY,
                            rotZ
                    )
            );
        }
        return LayerDefinition.create(mesh, textureWidth, textureHeight);
    }

    /**
     * Tiny Takeover Backport {@code BabyChickenModel.createBodyLayer}, used if
     * the Blockbench baby file is missing.
     */
    static LayerDefinition createBabyBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -2.25F, -0.75F, 4.0F, 4.0F, 4.0F)
                        .texOffs(10, 8).addBox(-1.0F, -0.25F, -1.75F, 2.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 20.25F, -1.25F)
        );
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                        .texOffs(2, 2).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 0.0F)
                        .texOffs(0, 1).addBox(-0.5F, 2.0F, -1.0F, 1.0F, 0.0F, 1.0F),
                PartPose.offset(1.0F, 22.0F, 0.5F)
        );
        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 2).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 0.0F)
                        .texOffs(0, 0).addBox(-0.5F, 2.0F, -1.0F, 1.0F, 0.0F, 1.0F),
                PartPose.offset(-1.0F, 22.0F, 0.5F)
        );
        root.addOrReplaceChild(
                "right_wing",
                CubeListBuilder.create().texOffs(6, 8).addBox(0.0F, 0.0F, -1.0F, 1.0F, 0.0F, 2.0F),
                PartPose.offset(2.0F, 20.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_wing",
                CubeListBuilder.create().texOffs(4, 8).addBox(-1.0F, 0.0F, -1.0F, 1.0F, 0.0F, 2.0F),
                PartPose.offset(-2.0F, 20.0F, 0.0F)
        );
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("beak", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("red_thing", CubeListBuilder.create(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 16, 16);
    }

    private static float[] javaPivotFromBlockbench(float[] origin) {
        return new float[] {origin[0], BLOCKBENCH_GROUND - origin[1], origin[2]};
    }

    private static float[] javaRotationFromBlockbench(float[] rotation) {
        return new float[] {-rotation[0], rotation[1], -rotation[2]};
    }

    private static float[] floats(JsonArray array, int count) {
        float[] values = new float[count];
        for (int i = 0; i < count; i++) {
            values[i] = array.get(i).getAsFloat();
        }
        return values;
    }

    private static float[] boxUvFromFaces(JsonObject cube) {
        if (!cube.has("faces") || !cube.get("faces").isJsonObject()) {
            return new float[] {0.0F, 0.0F};
        }
        JsonObject faces = cube.getAsJsonObject("faces");
        float minU = Float.POSITIVE_INFINITY;
        float minV = Float.POSITIVE_INFINITY;
        for (String face : new String[] {"up", "down", "north", "south", "east", "west"}) {
            if (!faces.has(face)) {
                continue;
            }
            JsonObject data = faces.getAsJsonObject(face);
            if (!data.has("uv")) {
                continue;
            }
            float[] uv = floats(data.getAsJsonArray("uv"), 4);
            minU = Math.min(minU, Math.min(uv[0], uv[2]));
            minV = Math.min(minV, Math.min(uv[1], uv[3]));
        }
        if (!Float.isFinite(minU) || !Float.isFinite(minV)) {
            return new float[] {0.0F, 0.0F};
        }
        return new float[] {minU, minV};
    }

    private static final class PartBuild {
        final String name;
        float[] pivot = new float[] {0.0F, 0.0F, 0.0F};
        float[] rotation;
        final List<CubeBuild> cubes = new ArrayList<>();

        PartBuild(String name) {
            this.name = name;
        }
    }

    private static final class CubeBuild {
        float[] uv = new float[] {0.0F, 0.0F};
        float[] from = new float[] {0.0F, 0.0F, 0.0F};
        float[] size = new float[] {1.0F, 1.0F, 1.0F};
        float inflate;
        boolean mirror;

        static CubeBuild fromBlockbench(JsonObject cube, float[] pivot) {
            CubeBuild build = new CubeBuild();
            float[] from = floats(cube.getAsJsonArray("from"), 3);
            float[] to = floats(cube.getAsJsonArray("to"), 3);
            float fromY = BLOCKBENCH_GROUND - to[1];
            float toY = BLOCKBENCH_GROUND - from[1];
            from[1] = fromY;
            to[1] = toY;
            build.from = new float[] {from[0] - pivot[0], from[1] - pivot[1], from[2] - pivot[2]};
            build.size = new float[] {to[0] - from[0], to[1] - from[1], to[2] - from[2]};
            if (cube.has("uv_offset")) {
                build.uv = floats(cube.getAsJsonArray("uv_offset"), 2);
            } else {
                build.uv = boxUvFromFaces(cube);
            }
            if (cube.has("inflate")) {
                build.inflate = cube.get("inflate").getAsFloat();
            }
            build.mirror = cube.has("mirror_uv") && cube.get("mirror_uv").getAsBoolean();
            return build;
        }

        static CubeBuild fromBedrock(JsonObject cube, float[] pivot) {
            CubeBuild build = new CubeBuild();
            float[] origin = floats(cube.getAsJsonArray("origin"), 3);
            build.size = floats(cube.getAsJsonArray("size"), 3);
            float javaOriginY = BLOCKBENCH_GROUND - (origin[1] + build.size[1]);
            build.from = new float[] {
                    origin[0] - pivot[0],
                    javaOriginY - pivot[1],
                    origin[2] - pivot[2]
            };
            if (cube.has("uv") && cube.get("uv").isJsonArray()) {
                build.uv = floats(cube.getAsJsonArray("uv"), 2);
            }
            if (cube.has("inflate")) {
                build.inflate = cube.get("inflate").getAsFloat();
            }
            build.mirror = cube.has("mirror") && cube.get("mirror").getAsBoolean();
            return build;
        }
    }
}
