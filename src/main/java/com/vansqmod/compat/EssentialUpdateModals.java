package com.vansqmod.compat;

/**
 * Class-name checks for Essential update UI so vansqmod need not compile against Essential.
 * Must live outside {@code com.vansqmod.mixin} — Mixin forbids loading non-mixin classes from
 * the mixin package when handlers run on a foreign (Essential) classloader path.
 */
public final class EssentialUpdateModals {

    private EssentialUpdateModals() {
    }

    public static boolean isUpdateModal(Object modal) {
        if (modal == null) {
            return false;
        }
        return switch (modal.getClass().getName()) {
            case "gg.essential.gui.modals.UpdateAvailableModal",
                 "gg.essential.gui.modals.UpdateNotificationModal",
                 "gg.essential.gui.modals.UpdateRequiredModal",
                 "gg.essential.gui.modals.EssentialRebootUpdateModal" -> true;
            default -> false;
        };
    }

    public static Object kotlinUnit() {
        try {
            return Class.forName("kotlin.Unit").getField("INSTANCE").get(null);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
