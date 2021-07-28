package su.nightexpress.quantumrpg.hooks.placeholders;

import me.clip.placeholderapi.PlaceholderAPI;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.Hook;

public class PapiHook extends Hook {
  public PapiHook(EHook type, QuantumRPG plugin) {
    super(type, plugin);
  }
  
  public void setup() {
    registerPlaceholderAPI();
  }
  
  public void shutdown() {}
  
  private void registerPlaceholderAPI() {
    PlaceholderAPI.registerExpansion(new PlaceholderAPIHook((QuantumRPG)this.plugin));
  }
}
