package su.nightexpress.quantumrpg.hooks.placeholders;

import java.util.Map;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.EntityAPI;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.types.ArmorType;
import su.nightexpress.quantumrpg.utils.Utils;

public class PlaceholderAPIHook extends PlaceholderExpansion {
  private QuantumRPG plugin;
  
  public PlaceholderAPIHook(QuantumRPG plugin) {
    this.plugin = plugin;
  }
  
  public String getAuthor() {
    return this.plugin.getDescription().getAuthors().get(0);
  }
  
  public String getIdentifier() {
    return "qrpg";
  }
  
  public String getPlugin() {
    return null;
  }
  
  public String getVersion() {
    return this.plugin.getDescription().getVersion();
  }
  
  public String onPlaceholderRequest(Player p, String tmp) {
    if (p == null || p.getInventory() == null)
      return null; 
    if (tmp.startsWith("itemstat_")) {
      String tt = tmp.replace("itemstat_", "");
      try {
        ItemStat type = ItemStat.valueOf(tt.toUpperCase());
        return String.valueOf(Utils.round3(EntityAPI.getItemStat((LivingEntity)p, type, null)));
      } catch (IllegalArgumentException ex) {
        return "Invalid item stat!";
      } 
    } 
    if (tmp.startsWith("damage_")) {
      String tt = tmp.replace("damage_", "");
      if (p == null || p.getInventory() == null)
        return "Error."; 
      return String.valueOf(Utils.round3(EntityAPI.getTotalDamageByType(tt, (LivingEntity)p)));
    } 
    if (tmp.startsWith("defense_")) {
      String tt = tmp.replace("defense_", "");
      ArmorType dt = Config.getArmorTypeById(tt);
      if (dt == null)
        return "Invalid Defense Type!"; 
      if (p == null || p.getInventory() == null)
        return "Error."; 
      Map<ArmorType, Double> map = EntityAPI.getDefenseTypes((LivingEntity)p, null);
      if (map.containsKey(dt))
        return String.valueOf(map.get(dt)); 
      return "0.0";
    } 
    return null;
  }
}
