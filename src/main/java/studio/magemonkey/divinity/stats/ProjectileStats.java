package studio.magemonkey.divinity.stats;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.stats.items.attributes.AmmoAttribute;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

public class ProjectileStats {

    private static Divinity plugin = Divinity.getInstance();

    private static final String PROJECTILE_SOURCE_WEAPON = "QRPG_PJ_WEAPON";
    private static final String PROJECTILE_PICKABLE      = "QRPG_PJ_PICKABLE";
    private static final String PROJECTILE_LAUNCH_POWER  = "QRPG_PJ_POWER";

    /**
     * The shooter's stats as they were when each projectile was launched. Kept here rather than in entity
     * metadata because metadata is only cleared when someone removes it, and these are not small: an entry is
     * released by {@link #sweep()} once its projectile is really gone.
     */
    private static final Map<UUID, Held> LAUNCH_SNAPSHOTS = new ConcurrentHashMap<>();

    /** Longest time a launch snapshot is kept whatever happens to its projectile: 10 minutes. */
    static final long MAX_HELD_MILLIS = 10 * 60 * 1000L;

    private static volatile LongSupplier clock = System::currentTimeMillis;

    private static final class Held {
        private final StatSnapshot snapshot;
        private final long         heldSince;
        private final UUID         world;
        private       int          chunkX;
        private       int          chunkZ;

        private Held(StatSnapshot snapshot, long heldSince, UUID world, int chunkX, int chunkZ) {
            this.snapshot = snapshot;
            this.heldSince = heldSince;
            this.world = world;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }
    }

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

    /**
     * Whether a projectile is one that can hurt what it hits, and so is worth taking its shooter's stats for.
     * Arrows, tridents and the damaging kinds of ammo Divinity can launch are; pearls, snowballs, eggs, bottles
     * and fishing hooks are not.
     */
    public static boolean canDealDamage(@NotNull Projectile pp) {
        if (pp instanceof AbstractArrow) return true; // Arrows, spectral arrows and tridents
        for (AmmoAttribute.Type type : AmmoAttribute.Type.values()) {
            if (type.dealsDamage() && type.getProjectileClass().isInstance(pp)) return true;
        }
        return false;
    }

    public static void setSnapshot(@NotNull Projectile pp, @NotNull StatSnapshot snapshot) {
        Location at = pp.getLocation();
        World    w  = at.getWorld();
        hold(pp.getUniqueId(),
                snapshot,
                w == null ? null : w.getUID(),
                at.getBlockX() >> 4,
                at.getBlockZ() >> 4);
    }

    static void hold(@NotNull UUID projectile, @NotNull StatSnapshot snapshot, @Nullable UUID world, int chunkX, int chunkZ) {
        LAUNCH_SNAPSHOTS.put(projectile, new Held(snapshot, clock.getAsLong(), world, chunkX, chunkZ));
    }

    /**
     * @return the shooter's stats at launch, or null if there are none: the projectile was not launched
     * through Divinity's launch event, it cannot deal damage, or its entry was released.
     */
    @Nullable
    public static StatSnapshot getSnapshot(@NotNull Projectile pp) {
        Held held = LAUNCH_SNAPSHOTS.get(pp.getUniqueId());
        return held == null ? null : held.snapshot;
    }

    /**
     * Releases the snapshots of projectiles that are gone. A projectile is gone when it can no longer be found
     * although the chunk it was last seen in is loaded, or when it is found dead. One that cannot be found
     * because its chunk is unloaded may come back, so it keeps its snapshot, up to {@link #MAX_HELD_MILLIS}
     * after launch, which also bounds anything else this fails to recognise.
     */
    public static void sweep() {
        long now = clock.getAsLong();
        LAUNCH_SNAPSHOTS.entrySet().removeIf(entry -> {
            Held held = entry.getValue();
            if (now - held.heldSince > MAX_HELD_MILLIS) return true;

            Entity entity = Bukkit.getEntity(entry.getKey());
            if (entity != null) {
                if (entity.isDead()) return true;
                Location at = entity.getLocation();
                held.chunkX = at.getBlockX() >> 4;
                held.chunkZ = at.getBlockZ() >> 4;
                return false;
            }

            World world = held.world == null ? null : Bukkit.getWorld(held.world);
            if (world == null) return true; // The world itself is gone
            return world.isChunkLoaded(held.chunkX, held.chunkZ);
        });
    }

    /** Releases every launch snapshot, for when the stats they were taken with are replaced. */
    public static void clear() {
        LAUNCH_SNAPSHOTS.clear();
    }

    static void setClock(@NotNull LongSupplier supplier) {
        clock = supplier;
    }

    static void resetClock() {
        clock = System::currentTimeMillis;
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
