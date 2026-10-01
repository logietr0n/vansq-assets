package com.vansqmod.compat;

import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.thatmaidenjaden.gleam.client.patcher.GleamVanillaPatcher;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.lwjgl.BufferUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Gleam only patches block rendertypes. Entity, item, and particle programs still
 * sample the vanilla lightmap, so they keep intensity but lose color. Reuse the
 * same GPU light grid already uploaded for terrain — one cell lookup per vertex.
 */
public final class GleamEntityShaders {

    private static final String MARKER = "vansqEntityGleam";
    private static final String GLEAM_CALL = "    vertexColor = computeLighting(pos, vertexColor, UV2);\n";
    private static final Set<String> PROGRAMS = Set.of(
            "particle",
            "rendertype_entity_solid",
            "rendertype_entity_cutout",
            "rendertype_entity_cutout_no_cull",
            "rendertype_entity_cutout_no_cull_z_offset",
            "rendertype_entity_translucent",
            "rendertype_entity_translucent_cull",
            "rendertype_entity_smooth_cutout",
            "rendertype_entity_no_outline",
            "rendertype_entity_decal",
            "rendertype_armor_cutout_no_cull",
            "rendertype_item_entity_translucent_cull",
            "rendertype_breeze_wind",
            "rendertype_crumbling",
            "translucent_no_light_direction"
    );
    private static final Matrix3f VIEW_ROT = new Matrix3f();
    private static final Quaternionf LAST_ROT = new Quaternionf();
    private static final FloatBuffer VIEW_ROT_BUFFER = BufferUtils.createFloatBuffer(9);
    private static final ThreadLocal<Boolean> GUI_ENTITY = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static boolean lastGui;
    private static boolean matrixReady;

    private GleamEntityShaders() {
    }

    public static boolean isProgram(String name) {
        String key = key(name);
        if (key.isEmpty() || isExcluded(key)) {
            return false;
        }
        if (PROGRAMS.contains(key)) {
            return true;
        }
        return key.startsWith("rendertype_entity_")
                && !key.contains("glint")
                && !key.contains("shadow")
                && !key.contains("eyes")
                && !key.contains("alpha")
                && !key.contains("emissive")
                && !key.contains("unlit");
    }

    public static void setGuiEntityLighting(boolean gui) {
        GUI_ENTITY.set(gui);
        matrixReady = false;
    }

    /** World entities and first-person hands: keep the camera rotation so Gleam runs. */
    public static void setWorldEntityLighting() {
        GUI_ENTITY.set(false);
        matrixReady = false;
    }

    public static void setFirstPersonHand(boolean hand) {
        if (hand) {
            setWorldEntityLighting();
        }
    }

    public static InputStream patchProgram(InputStream stream, Program.Type type, String name) {
        if (stream == null) {
            return stream;
        }
        if (!isProgram(name)) {
            return stream;
        }
        byte[] bytes;
        try {
            bytes = stream.readAllBytes();
        } catch (Exception e) {
            return stream;
        }
        try {
            String source = new String(bytes, StandardCharsets.UTF_8);
            if (type == Program.Type.VERTEX && (!hasUv2(source) || !source.contains("vertexColor"))) {
                return new ByteArrayInputStream(bytes);
            }
            if (type == Program.Type.FRAGMENT && !source.contains("fragColor")) {
                return new ByteArrayInputStream(bytes);
            }
            // Gleam's fragment patch samples Sampler0/texCoord0 for blacklight. Leash,
            // HSV splash, and other untextured programs do not have those. Compiling
            // that dumps resource packs ("could not reload shaders") and leaves the
            // loading overlay up. Vertex patches add v_GleamBlacklight, so skip those
            // too or the program fails to link on the same overlay pass.
            if (type == Program.Type.FRAGMENT
                    && (!source.contains("Sampler0") || !source.contains("texCoord0"))) {
                return new ByteArrayInputStream(bytes);
            }
            if (type == Program.Type.VERTEX && !source.contains("Sampler2")) {
                return new ByteArrayInputStream(bytes);
            }
            String patched = GleamVanillaPatcher.applyPatch(source, type);
            patched = fix(patched, type);
            if (patched.equals(source)) {
                return new ByteArrayInputStream(bytes);
            }
            return new ByteArrayInputStream(patched.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return new ByteArrayInputStream(bytes);
        }
    }

    public static String fix(String source, Program.Type type) {
        if (source == null || source.isEmpty() || source.contains(MARKER)) {
            return source;
        }
        if (type == Program.Type.VERTEX) {
            return fixVertex(source);
        }
        return source;
    }

    public static void uploadViewRotation(int location) {
        if (location < 0) {
            return;
        }
        ensureViewRotation();
        VIEW_ROT_BUFFER.clear();
        VIEW_ROT.get(VIEW_ROT_BUFFER);
        VIEW_ROT_BUFFER.rewind();
        RenderSystem.glUniformMatrix3(location, false, VIEW_ROT_BUFFER);
    }

    private static void ensureViewRotation() {
        boolean gui = Boolean.TRUE.equals(GUI_ENTITY.get());
        if (gui) {
            if (!matrixReady || !lastGui) {
                VIEW_ROT.zero();
                lastGui = true;
                matrixReady = true;
            }
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer == null ? null : minecraft.gameRenderer.getMainCamera();
        if (camera == null || !camera.isInitialized()) {
            if (!matrixReady || lastGui) {
                VIEW_ROT.zero();
                lastGui = false;
                matrixReady = true;
            }
            return;
        }
        Quaternionf rotation = camera.rotation();
        if (!matrixReady || lastGui || !LAST_ROT.equals(rotation)) {
            LAST_ROT.set(rotation);
            // camera.rotation() is local-to-world; the view matrix uses its conjugate.
            VIEW_ROT.rotation(rotation);
            lastGui = false;
            matrixReady = true;
        }
    }

    private static String fixVertex(String source) {
        if (!source.contains("in vec3 Position")
                || !source.contains("computeLighting")
                || source.contains("Position + ChunkOffset")) {
            return source;
        }
        boolean particle = isParticleVertex(source);
        if (!source.contains("IViewRotMat")) {
            source = source.replace("void main()", "uniform mat3 IViewRotMat;\n\nvoid main()");
        }
        String guarded = particle ? particleApply(source) : entityApply(source);
        if (source.contains(GLEAM_CALL)) {
            return source.replace(GLEAM_CALL, guarded);
        }
        int brace = source.lastIndexOf('}');
        if (brace < 0) {
            return source;
        }
        return source.substring(0, brace) + guarded + source.substring(brace);
    }

    /**
     * Living entities, items, and particles write camera-relative world positions.
     * Reconstructing through IViewRotMat * ModelViewMat double-rotates that and
     * misses the light cell — brightness remains (lightmap) but Gleam color does not.
     */
    private static boolean isParticleVertex(String source) {
        return hasUv2(source)
                && !source.contains("minecraft_mix_light")
                && !source.contains("ChunkOffset");
    }

    private static String particleApply(String source) {
        return "    // " + MARKER + "\n"
                + "    vec3 pos = Position;\n"
                + "    v_GleamBlacklight = 0.0;\n"
                + gleamApply(source);
    }

    private static String entityApply(String source) {
        return "    // " + MARKER + "\n"
                + "    vec3 pos = Position;\n"
                + "    v_GleamBlacklight = 0.0;\n"
                + "    if (dot(IViewRotMat[0], IViewRotMat[0]) > 0.5) {\n"
                + gleamApply(source)
                + "    }\n";
    }

    /**
     * Always add Gleam to the lightmap, never to vertex Color. Dyeable meshes
     * (leather, spawn eggs, wolf/horse armor, sheep wool, potions, etc.) bake the
     * tint into {@code vertexColor}; overlaying that washed the dye out.
     * <p>
     * Terrain Gleam adds onto biome-tinted vertices (luma ~0.5), so color shows in
     * daylight. Entity lightmaps are often 15/15 white; {@code SURFACE_LUM_CEILING}
     * then leaves almost no room to add. Evaluate against a mid-luma copy and add
     * only that extra onto the real lightmap.
     */
    private static String gleamApply(String source) {
        if (source.contains("out vec4 lightMapColor")) {
            String overlay = gleamOverlay("lightMapColor", lightCoord(source));
            if (source.contains("vertexColor = Color * lightMapColor")) {
                overlay += "        vertexColor = Color * lightMapColor;\n";
            }
            return overlay;
        }
        if (source.contains("texelFetch(Sampler2")) {
            return gleamBakedLightmap(source);
        }
        return gleamOverlay("vertexColor", lightCoord(source));
    }

    /**
     * {@code vertexColor} already includes {@code mix_light(Color) * lightmap}.
     * Un-multiply the original lightmap, gleam that sample, then re-multiply so
     * the dye/tint in Color is unchanged.
     */
    private static String gleamBakedLightmap(String source) {
        String lightCoord = lightCoord(source);
        return "        {\n"
                + "            vec4 vansqLm = texelFetch(Sampler2, UV2 / 16, 0);\n"
                + "            vec4 vansqGleamSrc = vansqLm;\n"
                + "            vec4 vansqGleamBase = vec4(vansqGleamSrc.rgb * 0.5, vansqGleamSrc.a);\n"
                + "            vec4 vansqGleam = computeLighting(pos, vansqGleamBase, " + lightCoord + ");\n"
                + "            vec3 vansqGleamedLm = clamp(vansqGleamSrc.rgb + (vansqGleam.rgb - vansqGleamBase.rgb), 0.0, 1.40);\n"
                + "            vec3 vansqLmSafe = max(vansqLm.rgb, vec3(0.001));\n"
                + "            vertexColor.rgb = (vertexColor.rgb / vansqLmSafe) * vansqGleamedLm;\n"
                + "        }\n";
    }

    private static String gleamOverlay(String target, String lightCoord) {
        return "        {\n"
                + "            vec4 vansqGleamSrc = " + target + ";\n"
                + "            vec4 vansqGleamBase = vec4(vansqGleamSrc.rgb * 0.5, vansqGleamSrc.a);\n"
                + "            vec4 vansqGleam = computeLighting(pos, vansqGleamBase, " + lightCoord + ");\n"
                + "            " + target + " = vec4(clamp(vansqGleamSrc.rgb + (vansqGleam.rgb - vansqGleamBase.rgb), 0.0, 1.40), vansqGleamSrc.a);\n"
                + "        }\n";
    }

    private static String lightCoord(String source) {
        if (hasUv2(source)
                && (source.contains("texelFetch(Sampler2")
                || source.contains("sample_lightmap")
                || source.contains("minecraft_sample_lightmap"))) {
            return "vec2(UV2)";
        }
        return "vec2(240.0)";
    }

    private static boolean isExcluded(String key) {
        return key.contains("glint")
                || key.contains("shadow")
                || key.contains("eyes")
                || key.contains("unlit")
                || key.contains("full_white")
                || key.contains("blit")
                || key.contains("cloud")
                || key.contains("end_portal")
                || key.contains("end_gateway")
                || key.contains("lightning")
                || key.contains("lines")
                || key.contains("outline")
                || key.contains("gui")
                || key.contains("text")
                || key.contains("water_mask")
                || key.contains("beacon")
                || key.contains("energy_swirl")
                || key.contains("leash")
                || key.contains("stars")
                || key.contains("hsv")
                || key.equals("position")
                || key.equals("position_color")
                || key.equals("position_tex")
                || key.equals("position_tex_color");
    }

    private static boolean hasUv2(String source) {
        return source.contains("in ivec2 UV2") || source.contains("in vec2 UV2");
    }

    private static String key(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        int colon = name.indexOf(':');
        if (colon >= 0) {
            name = name.substring(colon + 1);
        }
        int slash = name.lastIndexOf('/');
        return slash >= 0 ? name.substring(slash + 1) : name;
    }
}
