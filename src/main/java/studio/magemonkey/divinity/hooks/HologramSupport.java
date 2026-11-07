package studio.magemonkey.divinity.hooks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public final class HologramSupport {

    private HologramSupport() {
    }

    public static boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin(EHook.HOLOGRAPHIC_DISPLAYS) != null
                || Bukkit.getPluginManager().getPlugin(EHook.DECENT_HOLOGRAMS) != null;
    }

    public static Object create(Location loc, List<String> lines) {
        if (loc == null) return null;

        Plugin decent = Bukkit.getPluginManager().getPlugin(EHook.DECENT_HOLOGRAMS);
        if (decent != null) {
            try {
                Class<?> apiClass = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
                Method method = apiClass.getMethod("createHologram", String.class, Location.class, boolean.class, List.class);
                return method.invoke(null, "divinity-" + UUID.randomUUID(), loc, true, lines);
            } catch (Exception ignored) {
                try {
                    Class<?> apiClass = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
                    Method method = apiClass.getMethod("createHologram", String.class, Location.class, List.class);
                    return method.invoke(null, "divinity-" + UUID.randomUUID(), loc, lines);
                } catch (Exception ignored2) {
                    // fallback to legacy plugin in the next block
                }
            }
        }

        Plugin holographic = Bukkit.getPluginManager().getPlugin(EHook.HOLOGRAPHIC_DISPLAYS);
        if (holographic != null) {
            try {
                Class<?> apiClass = Class.forName("me.filoghost.holographicdisplays.api.HolographicDisplaysAPI");
                Method getMethod = apiClass.getMethod("get", Plugin.class);
                Object api = getMethod.invoke(null, holographic);
                Method createMethod = api.getClass().getMethod("createHologram", Location.class);
                Object hologram = createMethod.invoke(api, loc);
                setLines(hologram, lines);
                return hologram;
            } catch (Exception ignored) {
                // ignored, since caller can handle null.
            }
        }

        return null;
    }

    public static void setLines(Object hologram, List<String> lines) {
        if (hologram == null || lines == null) return;

        try {
            Class<?> apiClass = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
            Method method = apiClass.getMethod("setHologramLines", hologram.getClass(), List.class);
            method.invoke(null, hologram, lines);
            return;
        } catch (Exception ignored) {
            // try other API types
        }

        try {
            Object lineCollection = hologram.getClass().getMethod("getLines").invoke(hologram);
            Method clearMethod = lineCollection.getClass().getMethod("clear");
            clearMethod.invoke(lineCollection);
            Method appendMethod = lineCollection.getClass().getMethod("appendText", String.class);
            for (String line : lines) {
                appendMethod.invoke(lineCollection, line);
            }
        } catch (Exception ignored) {
            // ignored.
        }
    }

    public static void delete(Object hologram) {
        if (hologram == null) return;

        try {
            Method method = hologram.getClass().getMethod("delete");
            method.invoke(hologram);
        } catch (Exception ignored) {
            // ignored.
        }
    }

    public static Location getPosition(Object hologram) {
        if (hologram == null) return null;

        try {
            Method method = hologram.getClass().getMethod("getPosition");
            return (Location) method.invoke(hologram);
        } catch (Exception ignored) {
            try {
                Method method = hologram.getClass().getMethod("getLocation");
                return (Location) method.invoke(hologram);
            } catch (Exception ignored2) {
                return null;
            }
        }
    }

    public static void move(Object hologram, Location loc) {
        if (hologram == null || loc == null) return;

        try {
            Method method = hologram.getClass().getMethod("setPosition", Location.class);
            method.invoke(hologram, loc);
            return;
        } catch (Exception ignored) {
            // try other API types
        }

        try {
            Method method = hologram.getClass().getMethod("setLocation", Location.class);
            method.invoke(hologram, loc);
        } catch (Exception ignored) {
            // ignored.
        }
    }
}
