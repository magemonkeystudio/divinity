package studio.magemonkey.divinity.stats;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.divinity.Divinity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectileStats {

    private static Divinity plugin = Divinity.getInstance();

    private static final String PROJECTILE_SOURCE_WEAPON = "QRPG_PJ_WEAPON";
    private static final String PROJECTILE_PICKABLE      = "QRPG_PJ_PICKABLE";
    private static final String PROJECTILE_LAUNCH_POWER  = "QRPG_PJ_POWER";

    /**
     * The shooter's stats as they were when each projectile was launched. Kept here rather than in entity
     * metadata because metadata is only cleared when someone removes it, and these are not small: an entry is
     * released by {@link #sweep()} once its projectile is gone.
     */
    private static final Map<UUID, StatSnapshot> LAUNCH_SNAPSHOTS = new ConcurrentHashMap<>();

    public static void setSrcWeapon(@NotNull Projectile pp, @NotNull ItemStack item) {
        pp.setMetadata(PROJECTILE_SOURCE_WEAPON, new FixedMetadataValue(plugin, item));
    }

    @Nullable
    public static ItemStack getSrcWeapon(@NotNull Projectile e) {
        if (e instanceof Trident) {
            return ((Trident) e).getItem();
        }
        if (!e.hasMetadata(PROJECTILE_SOURCE_WEAPON)) return null;
        var meta = e.getMetadata(PROJECTILE_SOURCE_WEAPON);
        if (meta.isEmpty()) return null;
        Object val = meta.get(0).value();
        return (ItemStack) val;
    }

    public static void setPower(@NotNull Projectile e, double power) {
        e.setMetadata(PROJECTILE_LAUNCH_POWER, new FixedMetadataValue(plugin, power));
    }

    public static double getPower(@NotNull Projectile e) {
        if (!e.hasMetadata(PROJECTILE_LAUNCH_POWER)) return 1D;
        var meta = e.getMetadata(PROJECTILE_LAUNCH_POWER);
        if (meta.isEmpty()) return 1D;
        return meta.get(0).asDouble();
    }

    public static void setSnapshot(@NotNull Projectile pp, @NotNull StatSnapshot snapshot) {
        LAUNCH_SNAPSHOTS.put(pp.getUniqueId(), snapshot);
    }

    /**
     * @return the shooter's stats at launch, or null if there are none: the projectile was not launched
     * through Divinity's launch event, or its entry was released because it had left the world.
     */
    @Nullable
    public static StatSnapshot getSnapshot(@NotNull Projectile pp) {
        return LAUNCH_SNAPSHOTS.get(pp.getUniqueId());
    }

    /**
     * Releases the snapshots of projectiles that no longer exist (hit and removed, despawned, unloaded).
     */
    public static void sweep() {
        LAUNCH_SNAPSHOTS.keySet().removeIf(id -> {
            Entity entity = Bukkit.getEntity(id);
            return entity == null || !entity.isValid();
        });
    }

    /** How many projectiles have a snapshot held, for diagnostics and tests. */
    public static int snapshotCount() {
        return LAUNCH_SNAPSHOTS.size();
    }

    public static void setPickable(@NotNull Entity pp, boolean b) {
        pp.setMetadata(PROJECTILE_PICKABLE, new FixedMetadataValue(plugin, b));
    }

    public static boolean isPickable(@NotNull Entity pp) {
        if (!pp.hasMetadata(PROJECTILE_PICKABLE)) return true;
        var meta = pp.getMetadata(PROJECTILE_PICKABLE);
        if (meta.isEmpty()) return true;
        return meta.get(0).asBoolean();
    }
}
