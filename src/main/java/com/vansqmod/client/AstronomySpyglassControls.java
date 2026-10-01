package com.vansqmod.client;

import com.vansqmod.VansqMod;

import java.util.List;

/**
 * Spyglass Astronomy's attack click both drew constellation lines and selected
 * the nearest star for naming. Keep drawing on stars, and only select planets
 * or comets so {@code /sga:name} can name those plus constellations.
 */
public final class AstronomySpyglassControls {

    private static final String CLIENT = "com.nettakrim.spyglass_astronomy.SpyglassAstronomyClient";
    private static final String ASTRAL = "com.nettakrim.spyglass_astronomy.AstralObject";
    private static final String CONSTELLATION = "com.nettakrim.spyglass_astronomy.Constellation";

    private AstronomySpyglassControls() {
    }

    public static boolean isDrawing() {
        Object drawing = staticField(CLIENT, "isDrawingConstellation");
        return drawing instanceof Boolean flag && flag;
    }

    public static boolean nearestIsOrbitingBody() {
        Object nearest = nearestAstralObject();
        if (isNull(nearest)) {
            return false;
        }
        return !booleanField(nearest, "isStar");
    }

    public static void deselectStar() {
        try {
            Class.forName("com.nettakrim.spyglass_astronomy.Star").getMethod("deselect").invoke(null);
        } catch (Exception ignored) {
        }
    }

    public static void updateHover() {
        Object nearest = nearestAstralObject();
        if (isNull(nearest)) {
            return;
        }
        if (booleanField(nearest, "isStar")) {
            hoverConstellation(field(nearest, "star"));
            return;
        }
        hoverOrbitingBody(field(nearest, "orbitingBody"));
    }

    private static void hoverConstellation(Object star) {
        if (star == null) {
            return;
        }
        Object constellations = staticField(CLIENT, "constellations");
        if (!(constellations instanceof List<?> list)) {
            return;
        }
        for (Object constellation : list) {
            if (!invokeBoolean(constellation, "hasStar", star)) {
                continue;
            }
            if (invokeBoolean(constellation, "isUnnamed")) {
                if (constellation == staticField(CONSTELLATION, "selected")) {
                    sayActionBar("prompt.name.constellation");
                } else {
                    sayActionBar("prompt.unnamed.constellation");
                }
                return;
            }
            sayActionBar("prompt.constellation", field(constellation, "name"));
            return;
        }
    }

    private static void hoverOrbitingBody(Object body) {
        if (body == null) {
            return;
        }
        String type = booleanField(body, "isPlanet") ? "planet" : "comet";
        if (invokeBoolean(body, "isUnnamed")) {
            if (body == staticField(body.getClass().getName(), "selected")) {
                sayActionBar("prompt.name." + type);
            } else {
                sayActionBar("prompt.unnamed." + type);
            }
            return;
        }
        sayActionBar("prompt." + type, field(body, "name"));
    }

    private static Object nearestAstralObject() {
        try {
            java.lang.reflect.Method method = Class.forName(CLIENT)
                    .getDeclaredMethod("getNearestAstralObjectToCursor");
            method.setAccessible(true);
            return method.invoke(null);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isNull(Object nearest) {
        if (nearest == null) {
            return true;
        }
        try {
            return (Boolean) Class.forName(ASTRAL).getMethod("isNull", Class.forName(ASTRAL)).invoke(null, nearest);
        } catch (Exception e) {
            return true;
        }
    }

    private static void sayActionBar(String key, Object... args) {
        try {
            Class.forName(CLIENT).getMethod("sayActionBar", String.class, Object[].class).invoke(null, key, args);
        } catch (Exception e) {
            VansqMod.LOGGER.debug("Spyglass Astronomy hover prompt failed", e);
        }
    }

    private static Object field(Object instance, String name) {
        try {
            return instance.getClass().getField(name).get(instance);
        } catch (Exception e) {
            return null;
        }
    }

    private static Object staticField(String className, String name) {
        try {
            return Class.forName(className).getField(name).get(null);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean booleanField(Object instance, String name) {
        Object value = field(instance, name);
        return value instanceof Boolean flag && flag;
    }

    private static boolean invokeBoolean(Object instance, String method, Object... args) {
        try {
            for (java.lang.reflect.Method candidate : instance.getClass().getMethods()) {
                if (!candidate.getName().equals(method) || candidate.getParameterCount() != args.length) {
                    continue;
                }
                Object value = candidate.invoke(instance, args);
                return value instanceof Boolean flag && flag;
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
