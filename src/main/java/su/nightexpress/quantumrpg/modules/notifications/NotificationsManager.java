package su.nightexpress.quantumrpg.modules.notifications;

import java.util.HashMap;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.api.events.QuantumItemDamageEvent;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;

public class NotificationsManager extends QModule {
  private HashMap<Double, String> dur_perc;
  
  public NotificationsManager(QuantumRPG plugin, boolean enabled, MExecutor exe) {
    super(plugin, enabled, exe);
  }
  
  public EModule type() {
    return EModule.NOTIFICATIONS;
  }
  
  public String name() {
    return "Notifications";
  }
  
  public String version() {
    return "1.0";
  }
  
  public boolean isDropable() {
    return false;
  }
  
  public boolean isResolvable() {
    return false;
  }
  
  public void updateCfg() {}
  
  public void setup() {
    JYML jYML = this.cfg.getConfig();
    this.dur_perc = new HashMap<>();
    if (jYML.contains("durability.on-percentage"))
      for (String o : jYML.getConfigurationSection("durability.on-percentage").getKeys(false)) {
        double i = Double.parseDouble(o.toString());
        String s = ChatColor.translateAlternateColorCodes('&', jYML.getString("durability.on-percentage." + o));
        this.dur_perc.put(Double.valueOf(i), s);
      }  
  }
  
  public void shutdown() {
    this.dur_perc = null;
  }
  
  public void notifyDurability(Player p, ItemStack i) {
    double d = ItemAPI.getDurabilityMinOrMax(i, 0) / ItemAPI.getDurabilityMinOrMax(i, 1) * 100.0D;
    if (this.dur_perc.containsKey(Double.valueOf(d))) {
      String s = ((String)this.dur_perc.get(Double.valueOf(d))).replace("%d1", String.valueOf(ItemAPI.getDurabilityMinOrMax(i, 0) - 1)).replace("%d2", (new StringBuilder(String.valueOf(ItemAPI.getDurabilityMinOrMax(i, 1)))).toString());
      out((Entity)p, s);
    } 
  }
  
  @EventHandler
  public void onDmg(QuantumItemDamageEvent e) {
    LivingEntity li = e.getEntity();
    if (!(li instanceof Player))
      return; 
    Player p = (Player)li;
    ItemStack i = e.getItem();
    notifyDurability(p, i);
  }
}
