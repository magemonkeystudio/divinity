package su.nightexpress.quantumrpg.libs.packetlistener;

import org.bukkit.plugin.Plugin;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.libs.apihelper.APIManager;

public class PacketListenerPlugin {
  private QuantumRPG plugin;
  
  private PacketListenerAPI packetListenerAPI;
  
  public PacketListenerPlugin(QuantumRPG plugin) {
    this.plugin = plugin;
    this.packetListenerAPI = new PacketListenerAPI();
  }
  
  public void setup() {
    APIManager.registerAPI(this.packetListenerAPI, (Plugin)this.plugin);
    APIManager.initAPI(PacketListenerAPI.class);
  }
  
  public void disable() {
    this.packetListenerAPI.disable((Plugin)this.plugin);
    APIManager.disableAPI(PacketListenerAPI.class);
  }
  
  public PacketListenerAPI getPLA() {
    return this.packetListenerAPI;
  }
}
