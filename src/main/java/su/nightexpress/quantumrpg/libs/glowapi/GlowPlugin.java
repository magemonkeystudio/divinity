package su.nightexpress.quantumrpg.libs.glowapi;

import org.bukkit.plugin.Plugin;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.libs.apihelper.APIManager;

public class GlowPlugin {
  private QuantumRPG plugin;
  
  private GlowAPI glowAPI;
  
  public GlowPlugin(QuantumRPG plugin) {
    this.plugin = plugin;
  }
  
  public void setup() {
    this.glowAPI = new GlowAPI();
    APIManager.registerAPI(this.glowAPI, (Plugin)this.plugin);
    APIManager.initAPI(GlowAPI.class);
  }
  
  public void disable() {
    APIManager.disableAPI(GlowAPI.class);
  }
}
