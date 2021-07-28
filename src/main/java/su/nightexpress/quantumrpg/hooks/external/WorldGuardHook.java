package su.nightexpress.quantumrpg.hooks.external;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.Hook;

public class WorldGuardHook extends Hook {
    private WorldGuardPlugin wg;

    public WorldGuardHook(EHook type, QuantumRPG plugin) {
        super(type, plugin);
    }

    public void setup() {
        this.wg = (WorldGuardPlugin) this.plugin.getPluginManager().getPlugin(this.type.getPluginName());
    }

    public void shutdown() {
    }

    public boolean canFights(Entity e1, Entity e2) {
        if (!(e1 instanceof Player) || !(e2 instanceof Player))
            return true;

        RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(e1.getWorld()));
        if (rm == null)
            return true;
        ApplicableRegionSet set1 = rm.getApplicableRegions(convertToSk89qBV(e1.getLocation()));
        ApplicableRegionSet set2 = rm.getApplicableRegions(convertToSk89qBV(e2.getLocation()));
        return !((set1 != null || set2 != null) && (set1.queryState(null, new StateFlag[]{Flags.PVP}) == StateFlag.State.DENY || set2.queryState(null, new StateFlag[]{Flags.PVP}) == StateFlag.State.DENY));
    }

    private BlockVector3 convertToSk89qBV(Location location) {
        return BlockVector3.at(location.getX(), location.getY(), location.getZ());
    }

    public boolean canBuilds(Player p) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer()
                .createQuery()
                .testState(BukkitAdapter.adapt(p.getLocation()), WorldGuardPlugin.inst().wrapPlayer(p), Flags.BUILD);
    }

    public boolean isInRegion(LivingEntity li, String region) {
        return getRegion(li).equalsIgnoreCase(region);
    }

    public String getRegion(LivingEntity li) {
        RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer()
                .get(BukkitAdapter.adapt(li.getWorld()));
        ApplicableRegionSet set = rm.getApplicableRegions(convertToSk89qBV(li.getLocation()));
        String reg = "";
        int i = -1;
        for (ProtectedRegion pr : set) {
            if (pr.getPriority() > i) {
                i = pr.getPriority();
                reg = pr.getId();
            }
        }
        return reg;
    }
}
