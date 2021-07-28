package su.nightexpress.quantumrpg.hooks.external;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.Hook;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HDHook extends Hook {
    private Map<Hologram, Integer> map;

    private int taskId;

    public HDHook(EHook type, QuantumRPG plugin) {
        super(type, plugin);
    }

    public void setup() {
        this.map = new HashMap<>();
        start();
    }

    public void shutdown() {
        this.plugin.getServer().getScheduler().cancelTask(this.taskId);
        for (Hologram h : this.map.keySet())
            h.delete();
        this.map = null;
    }

    public void createIndicator(Location loc, List<String> list) {
        Location l2 = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
        Hologram holo = HologramsAPI.createHologram(this.plugin, l2);
        for (String s : list)
            holo.appendTextLine(s);
        this.map.put(holo, Integer.valueOf(1));
    }

    public boolean isHologram(Entity e) {
        return HologramsAPI.isHologramEntity(e);
    }

    private void up(Hologram h) {
        int i = 1;
        if (this.map.containsKey(h))
            i = this.map.get(h).intValue();
        h.teleport(h.getLocation().add(0.0D, 0.1D + (i / 15), 0.0D));
        i++;
        if (i >= 15) {
            this.map.remove(h);
            h.delete();
        } else {
            this.map.put(h, Integer.valueOf(i));
        }
    }

    public Map<Hologram, Integer> getHolomap() {
        return this.map;
    }

    public void start() {
        this.taskId = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {
            public void run() {
                if (HDHook.this.getHolomap().size() > 0) {
                    Map<Hologram, Integer> map = new HashMap<>(HDHook.this.getHolomap());
                    for (Hologram h : map.keySet())
                        HDHook.this.up(h);
                }
            }
        }, 10L, 1L);
    }
}
