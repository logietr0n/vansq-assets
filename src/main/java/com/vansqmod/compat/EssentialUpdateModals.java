package com.vansqmod.compat;

/**
 * Class-name checks for Essential update UI so vansqmod need not compile against Essential.
 * <p>
 * Essential mixins must not call this class: injected handlers run on Essential's
 * classloader, which cannot load {@code com.vansqmod.compat} types. Keep the checks
 * inlined on those mixins instead.
 */
public final class EssentialUpdateModals {

    private EssentialUpdateModals() {
    }

    public static boolean isUpdateModal(Object modal) {
        if (modal == null) {
            return false;
        }
        String name = modal.getClass().getName();
        if (!name.startsWith("gg.essential.")) {
            return false;
        }
        int dot = name.lastIndexOf('.');
        String simple = dot >= 0 ? name.substring(dot + 1) : name;
        return simple.contains("AutoInstalled")
                || (simple.contains("Update") && simple.contains("Modal"));
    }

    public static Object kotlinUnit() {
        try {
            return Class.forName("kotlin.Unit").getField("INSTANCE").get(null);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
