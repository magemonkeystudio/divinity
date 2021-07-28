package su.nightexpress.quantumrpg;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public abstract class QListener<P extends Plugin> implements Listener {
  public final P plugin;
  
  public QListener(P plugin) {
    this.plugin = plugin;
  }
  
  public void registerListeners() {
    this.plugin.getServer().getPluginManager().registerEvents(this, (Plugin)this.plugin);
  }
  
  public void unregisterListeners() {
    HandlerList.unregisterAll(this);
  }
}
