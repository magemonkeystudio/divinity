package su.nightexpress.quantumrpg.tasks;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.utils.MetaUtils;

public class AtkTask {
  private QuantumRPG plugin;
  
  private int id;
  
  public AtkTask(QuantumRPG plugin) {
    this.plugin = plugin;
  }
  
  public void stop() {
    this.plugin.getServer().getScheduler().cancelTask(this.id);
  }
  
  public void start() {
    this.id = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, new Runnable() {
          public void run() {
            for (Player p : AtkTask.this.plugin.getServer().getOnlinePlayers())
              MetaUtils.storeAtkPower(p); 
          }
        },  0L, 1L);
  }
}
