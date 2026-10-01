package com.vansqmod.compat;

import com.vansqmod.VansqMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Essential rebuilds its title/pause/options proxy widgets on every {@code initGui}.
 * FancyMenu identifies those proxies by a hash of their <em>identity</em> coordinates
 * ({@code initialPosId * 2}+{@code initialPosId}), then stores a {@code WidgetMeta}
 * pointing at that specific widget instance.
 * <p>
 * After leaving the title screen and coming back, Essential creates new proxy objects
 * and a new overlay. FancyMenu can still place the hidden proxy widgets, but Essential
 * resets {@code proxyInControl} and draws its real buttons at the default sidebar.
 * Rebind the live proxies and force Essential to follow those FancyMenu positions.
 */
@EventBusSubscriber(modid = VansqMod.MODID, value = Dist.CLIENT)
public final class EssentialFancyMenuLayout {

    /**
     * Essential {@code ScreenWithProxiesHandler} identity ids. FancyMenu's numeric
     * widget id is {@code String.valueOf(id * 2) + id} (concatenated x/y).
     */
    private static final Map<String, Integer> ESSENTIAL_POS_IDS = Map.ofEntries(
            Map.entry("invite_host", 2),
            Map.entry("world_host", 3),
            Map.entry("social", 4),
            Map.entry("wardrobe", 5),
            Map.entry("wardrobe_2", 6),
            Map.entry("pictures", 7),
            Map.entry("settings", 8),
            Map.entry("account", 9),
            Map.entry("beta", 11),
            Map.entry("update", 12),
            Map.entry("message", 13),
            Map.entry("player", 14)
    );

    private static boolean lookupFailed;
    private static Method getLayerOfScreen;
    private static Field vanillaWidgetElements;
    private static Constructor<?> widgetMetaCtor;
    private static Method getInstanceIdentifier;
    private static Method getWidget;
    private static Method setVanillaWidget;
    private static Method updateWidgetPosition;
    private static Method updateWidgetSize;
    private static Method updateWidgetVisibility;
    private static Method setCustomX;
    private static Method setCustomY;
    private static Method setCustomWidth;
    private static Method setCustomHeight;
    private static Method getAbsoluteX;
    private static Method getAbsoluteY;
    private static Method getAbsoluteWidth;
    private static Method getAbsoluteHeight;
    private static Method getCustomX;
    private static Field proxyInControlField;

    private EssentialFancyMenuLayout() {
    }

    public static void rebindFromProxyHandler(Object handler) {
        if (handler == null || !fancymenuLoaded()) {
            return;
        }
        Screen screen = readScreen(handler);
        if (screen == null) {
            screen = Minecraft.getInstance().screen;
        }
        if (screen == null) {
            return;
        }
        rebind(screen);
    }

    public static void rebindFromFancyMenuEvent(Object event) {
        if (event == null || !fancymenuLoaded()) {
            return;
        }
        try {
            Screen screen = (Screen) event.getClass().getMethod("getScreen").invoke(event);
            rebind(screen);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    public static void rebindCurrentScreen() {
        Screen screen = Minecraft.getInstance().screen;
        if (screen != null) {
            rebind(screen);
        }
    }

    /**
     * Essential resets {@code proxyInControl} whenever it re-attaches its overlay.
     * If FancyMenu has already customized this proxy, take control back so the overlay
     * follows the layout instead of the default sidebar.
     */
    public static void takeControlIfCustomized(AbstractWidget widget) {
        if (widget == null || !hasCustomFancyMenuPos(widget)) {
            return;
        }
        setProxyInControl(widget, true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onScreenRender(ScreenEvent.Render.Pre event) {
        Screen screen = event.getScreen();
        if (!isEssentialProxyScreen(screen)) {
            return;
        }
        rebind(screen);
    }

    static void rebind(Screen screen) {
        if (screen == null || lookupFailed || !fancymenuLoaded()) {
            return;
        }
        // FancyMenu's identification pass inits the screen at 1000x1000; leave those
        // identity-position proxies alone so ids stay stable.
        if (screen.width == 1000 && screen.height == 1000) {
            return;
        }
        if (!ensureLookups()) {
            return;
        }
        try {
            Object layer = getLayerOfScreen.invoke(null, screen);
            if (layer == null) {
                return;
            }
            Object rawElements = vanillaWidgetElements.get(layer);
            if (!(rawElements instanceof List<?> elements) || elements.isEmpty()) {
                return;
            }
            Map<String, AbstractWidget> proxies = indexEssentialProxies(screen);
            if (proxies.isEmpty()) {
                return;
            }
            for (Object element : elements) {
                if (element == null) {
                    continue;
                }
                String fancyId = normalizeIdentifier(String.valueOf(getInstanceIdentifier.invoke(element)));
                AbstractWidget live = proxies.get(fancyId);
                if (live == null) {
                    continue;
                }
                Object current = getWidget.invoke(element);
                if (current != live) {
                    long id;
                    try {
                        id = Long.parseLong(fancyId);
                    } catch (NumberFormatException ignored) {
                        continue;
                    }
                    Object meta = widgetMetaCtor.newInstance(live, id, screen);
                    setVanillaWidget.invoke(element, meta, Boolean.FALSE);
                }
                updateWidgetPosition.invoke(element);
                updateWidgetSize.invoke(element);
                updateWidgetVisibility.invoke(element);
                applyCustomBounds(element, live);
                takeControlIfCustomized(live);
            }
        } catch (ReflectiveOperationException | RuntimeException ex) {
            VansqMod.LOGGER.debug("FancyMenu Essential button rebind skipped: {}", ex.toString());
        }
    }

    private static void applyCustomBounds(Object element, AbstractWidget live) {
        if (setCustomX == null) {
            return;
        }
        try {
            int x = (Integer) getAbsoluteX.invoke(element);
            int y = (Integer) getAbsoluteY.invoke(element);
            int width = (Integer) getAbsoluteWidth.invoke(element);
            int height = (Integer) getAbsoluteHeight.invoke(element);
            setCustomX.invoke(live, x);
            setCustomY.invoke(live, y);
            setCustomWidth.invoke(live, width);
            setCustomHeight.invoke(live, height);
            live.setX(x);
            live.setY(y);
            live.setWidth(width);
            live.setHeight(height);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static boolean isEssentialProxyScreen(Screen screen) {
        return screen instanceof TitleScreen
                || screen instanceof PauseScreen
                || screen instanceof OptionsScreen;
    }

    private static boolean hasCustomFancyMenuPos(AbstractWidget widget) {
        try {
            Method getter = getCustomX;
            if (getter == null) {
                getter = widget.getClass().getMethod("getCustomXFancyMenu");
            }
            return getter.invoke(widget) != null;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static void setProxyInControl(AbstractWidget widget, boolean value) {
        try {
            Field field = proxyInControlField;
            if (field == null) {
                Class<?> type = widget.getClass();
                while (type != null && type != Object.class) {
                    try {
                        field = type.getDeclaredField("proxyInControl");
                        field.setAccessible(true);
                        proxyInControlField = field;
                        break;
                    } catch (NoSuchFieldException ignored) {
                        type = type.getSuperclass();
                    }
                }
            }
            if (field != null) {
                field.setBoolean(widget, value);
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static Map<String, AbstractWidget> indexEssentialProxies(Screen screen) {
        Map<String, AbstractWidget> byId = new HashMap<>();
        for (GuiEventListener child : screen.children()) {
            if (!(child instanceof AbstractWidget widget)) {
                continue;
            }
            String message = widget.getMessage() == null ? "" : widget.getMessage().getString();
            if (!message.startsWith("<essential_") || !message.endsWith(">")) {
                continue;
            }
            String essentialId = message.substring("<essential_".length(), message.length() - 1);
            Integer posId = ESSENTIAL_POS_IDS.get(essentialId);
            if (posId == null) {
                continue;
            }
            byId.put(String.valueOf(posId * 2) + posId, widget);
        }
        return byId;
    }

    private static String normalizeIdentifier(String id) {
        if (id == null) {
            return "";
        }
        return id.replace("vanillabtn:", "").replace("button_compatibility_id:", "").trim();
    }

    private static Screen readScreen(Object handler) {
        Class<?> type = handler.getClass();
        while (type != null && type != Object.class) {
            try {
                Field field = type.getDeclaredField("screen");
                field.setAccessible(true);
                Object value = field.get(handler);
                return value instanceof Screen s ? s : null;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (IllegalAccessException ex) {
                return null;
            }
        }
        return null;
    }

    private static boolean fancymenuLoaded() {
        return ModList.get().isLoaded("fancymenu");
    }

    private static boolean ensureLookups() {
        if (getLayerOfScreen != null) {
            return true;
        }
        if (lookupFailed) {
            return false;
        }
        try {
            Class<?> handler = Class.forName("de.keksuccino.fancymenu.customization.layer.ScreenCustomizationLayerHandler");
            Class<?> layer = Class.forName("de.keksuccino.fancymenu.customization.layer.ScreenCustomizationLayer");
            Class<?> element = Class.forName("de.keksuccino.fancymenu.customization.element.elements.button.vanillawidget.VanillaWidgetElement");
            Class<?> abstractElement = Class.forName("de.keksuccino.fancymenu.customization.element.AbstractElement");
            Class<?> widgetMeta = Class.forName("de.keksuccino.fancymenu.customization.widget.WidgetMeta");
            getLayerOfScreen = handler.getMethod("getLayerOfScreen", Screen.class);
            vanillaWidgetElements = layer.getField("vanillaWidgetElements");
            widgetMetaCtor = widgetMeta.getConstructor(AbstractWidget.class, long.class, Screen.class);
            getInstanceIdentifier = element.getMethod("getInstanceIdentifier");
            getWidget = element.getMethod("getWidget");
            setVanillaWidget = element.getMethod("setVanillaWidget", widgetMeta, boolean.class);
            updateWidgetPosition = element.getMethod("updateWidgetPosition");
            updateWidgetSize = element.getMethod("updateWidgetSize");
            updateWidgetVisibility = element.getMethod("updateWidgetVisibility");
            getAbsoluteX = abstractElement.getMethod("getAbsoluteX");
            getAbsoluteY = abstractElement.getMethod("getAbsoluteY");
            getAbsoluteWidth = abstractElement.getMethod("getAbsoluteWidth");
            getAbsoluteHeight = abstractElement.getMethod("getAbsoluteHeight");
            Class<?> customizable = Class.forName("de.keksuccino.fancymenu.util.rendering.ui.widget.CustomizableWidget");
            setCustomX = customizable.getMethod("setCustomXFancyMenu", Integer.class);
            setCustomY = customizable.getMethod("setCustomYFancyMenu", Integer.class);
            setCustomWidth = customizable.getMethod("setCustomWidthFancyMenu", Integer.class);
            setCustomHeight = customizable.getMethod("setCustomHeightFancyMenu", Integer.class);
            getCustomX = customizable.getMethod("getCustomXFancyMenu");
            return true;
        } catch (ReflectiveOperationException ex) {
            lookupFailed = true;
            VansqMod.LOGGER.debug("FancyMenu Essential layout hooks unavailable: {}", ex.toString());
            return false;
        }
    }
}
