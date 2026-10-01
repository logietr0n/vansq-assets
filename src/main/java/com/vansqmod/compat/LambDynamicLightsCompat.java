package com.vansqmod.compat;

import net.minecraft.world.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * Optional bridge to LambDynamicLights. Resolved at runtime so Gleam still works
 * when the dynamic-light mod is absent.
 */
public final class LambDynamicLightsCompat {

    private static final Object LOCK = new Object();
    private static volatile MethodHandle getInstance;
    private static volatile MethodHandle getLuminanceFrom;
    private static volatile MethodHandle entityGetLuminance;
    private static volatile boolean entityLuminanceMissing;
    private static volatile boolean missing;

    private LambDynamicLightsCompat() {
    }

    public static int getEntityLuminance(Entity entity) {
        if (entity == null || missing) {
            return 0;
        }
        int live = invokeEntityLuminance(entity);
        MethodHandle fromHandle = resolveLuminanceFrom();
        int fromApi = 0;
        if (fromHandle != null) {
            try {
                fromApi = (int) fromHandle.invoke(entity);
            } catch (Throwable ignored) {
            }
        }
        return Math.max(live, fromApi);
    }

    private static int invokeEntityLuminance(Entity entity) {
        MethodHandle handle = entityGetLuminance;
        if (handle == null && !entityLuminanceMissing) {
            handle = bindEntityLuminance();
        }
        if (handle == null) {
            return 0;
        }
        try {
            return (int) handle.invoke(entity);
        } catch (Throwable ignored) {
            return 0;
        }
    }

    private static MethodHandle bindEntityLuminance() {
        synchronized (LOCK) {
            if (entityGetLuminance != null) {
                return entityGetLuminance;
            }
            try {
                Method method = Entity.class.getMethod("getLuminance");
                if (method.getReturnType() == int.class) {
                    entityGetLuminance = MethodHandles.publicLookup().unreflect(method);
                    return entityGetLuminance;
                }
            } catch (Throwable ignored) {
            }
            entityLuminanceMissing = true;
            return null;
        }
    }

    private static MethodHandle resolveInstance() {
        MethodHandle handle = getInstance;
        if (handle != null || missing) {
            return handle;
        }
        synchronized (LOCK) {
            if (getInstance != null || missing) {
                return getInstance;
            }
            try {
                Class<?> cls = Class.forName("dev.lambdaurora.lambdynlights.LambDynLights");
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                getInstance = lookup.findStatic(cls, "get", MethodType.methodType(cls));
                bindLuminanceFrom(cls, lookup);
                return getInstance;
            } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                missing = true;
                return null;
            } catch (Throwable ignored) {
                return null;
            }
        }
    }

    private static MethodHandle resolveLuminanceFrom() {
        MethodHandle handle = getLuminanceFrom;
        if (handle != null || missing) {
            return handle;
        }
        resolveInstance();
        return getLuminanceFrom;
    }

    private static void bindLuminanceFrom(Class<?> cls, MethodHandles.Lookup lookup) {
        try {
            getLuminanceFrom = lookup.findStatic(cls, "getLuminanceFrom", MethodType.methodType(int.class, Entity.class));
            return;
        } catch (Throwable ignored) {
        }
        try {
            for (Method method : cls.getMethods()) {
                if ("getLuminanceFrom".equals(method.getName()) && method.getParameterCount() == 1
                        && Entity.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    getLuminanceFrom = lookup.unreflect(method);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
