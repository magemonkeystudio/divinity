package su.nightexpress.quantumrpg.tasks;

import org.bukkit.plugin.Plugin;
import su.nightexpress.quantumrpg.QuantumRPG;

public class BleedTask {
  private QuantumRPG plugin;
  
  private int id;
  
  public BleedTask(QuantumRPG plugin) {
    this.plugin = plugin;
  }
  
  public void stop() {
    this.plugin.getServer().getScheduler().cancelTask(this.id);
  }
  
  public void start() {
    this.id = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, new Runnable() {
          public void run() {
            BleedTask.this.plugin.getTM().processBleed();
          }
        },  0L, 20L);
  }
}
