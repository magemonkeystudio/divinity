package su.nightexpress.quantumrpg.libs.apihelper;

import org.bukkit.plugin.Plugin;

public interface API {
  void load();
  
  void init(Plugin paramPlugin);
  
  void disable(Plugin paramPlugin);
}
