//package su.nightexpress.quantumrpg.hooks.external;
//
//import com.zthana.racesofthana.Race;
//import com.zthana.racesofthana.RacesOfThana;
//import org.bukkit.ChatColor;
//import org.bukkit.entity.Player;
//import su.nightexpress.quantumrpg.QuantumRPG;
//import su.nightexpress.quantumrpg.hooks.EHook;
//import su.nightexpress.quantumrpg.hooks.Hook;
//import su.nightexpress.quantumrpg.hooks.HookClass;
//
//public class RoTHook extends Hook implements HookClass {
//  private RacesOfThana races;
//
//  public RoTHook(EHook type, QuantumRPG plugin) {
//    super(type, plugin);
//  }
//
//  public void setup() {
//    this.races = (RacesOfThana)((QuantumRPG)this.plugin).getPluginManager().getPlugin(this.type.getPluginName());
//  }
//
//  public void shutdown() {}
//
//  public String getClass(Player p) {
//    Race r = this.races.getRaceHandler().getRace(p);
//    if (r == null)
//      return "None";
//    return ChatColor.stripColor(r.getName());
//  }
//}
