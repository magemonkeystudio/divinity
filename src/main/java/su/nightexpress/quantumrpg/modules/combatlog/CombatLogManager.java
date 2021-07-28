package su.nightexpress.quantumrpg.modules.combatlog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.external.HDHook;
import su.nightexpress.quantumrpg.listeners.DamageMeta;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.utils.MetaUtils;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;

public class CombatLogManager extends QModule {
  private boolean ignore_zero;
  
  private boolean ind_enabled;
  
  private List<String> ind_f_order;
  
  private HashMap<String, String> ind_f_dt;
  
  private HashMap<String, String> ind_f_ds;
  
  public CombatLogManager(QuantumRPG plugin, boolean enabled, MExecutor exec) {
    super(plugin, enabled, exec);
    this.plugin = plugin;
  }
  
  public EModule type() {
    return EModule.COMBAT_LOG;
  }
  
  public String name() {
    return "Combat Log";
  }
  
  public String version() {
    return "1.0.0";
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
    String path = "messages.";
    this.ignore_zero = jYML.getBoolean(String.valueOf(path) + "ignore-zero-damage");
    byte b;
    int i;
    MessageType[] arrayOfMessageType;
    for (i = (arrayOfMessageType = MessageType.values()).length, b = 0; b < i; ) {
      MessageType mt = arrayOfMessageType[b];
      String path2 = String.valueOf(path) + "types." + mt.name() + ".";
      boolean e = jYML.getBoolean(String.valueOf(path2) + "enabled");
      String msg_d = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path2) + "msg-to-damager"));
      String msg_r = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path2) + "msg-to-receiver"));
      String msg_i = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path2) + "msg-indicator"));
      Sound s = null;
      String ss = jYML.getString(String.valueOf(path2) + "sound").toUpperCase();
      if (!ss.equalsIgnoreCase("null") && !ss.equalsIgnoreCase("none"))
        try {
          s = Sound.valueOf(ss);
        } catch (IllegalArgumentException ex) {
          log("Invalid sound '" + ss + "' for '" + mt.name() + "' message! Use 'NULL' to disable sound and skip this warn.", LogType.WARN);
        }  
      mt.setEnabled(e);
      mt.setMsgDamager(msg_d);
      mt.setMsgReceiver(msg_r);
      mt.setMsgIndicator(msg_i);
      mt.setSound(s);
      b++;
    } 
    path = "indicators.";
    this.ind_enabled = (jYML.getBoolean(String.valueOf(path) + "enabled") && EHook.HOLOGRAPHIC_DISPLAYS.isEnabled());
    this.ind_f_order = jYML.getStringList(String.valueOf(path) + "format.order");
    this.ind_f_dt = new HashMap<>();
    if (jYML.isConfigurationSection(String.valueOf(path) + "format.damage-types"))
      for (String s : jYML.getConfigurationSection(String.valueOf(path) + "format.damage-types").getKeys(false)) {
        String s2 = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "format.damage-types." + s));
        this.ind_f_dt.put(s.toLowerCase(), s2);
      }  
    this.ind_f_ds = new HashMap<>();
    if (jYML.isConfigurationSection(String.valueOf(path) + "format.damage-sources"))
      for (String s : jYML.getConfigurationSection(String.valueOf(path) + "format.damage-sources").getKeys(false)) {
        String s2 = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "format.damage-sources." + s));
        this.ind_f_ds.put(s.toUpperCase(), s2);
      }  
  }
  
  public void shutdown() {
    this.ind_f_order = null;
    this.ind_f_dt = null;
    this.ind_f_ds = null;
  }
  
  public void sendCombatMsg(MessageType type, double dmg, double block, String rec_name, String d_name, Entity rec, Entity dd) {
    if (!type.isEnabled())
      return; 
    if (dmg <= 0.0D && this.ignore_zero)
      return; 
    String s_dmg = String.valueOf(Utils.round3(dmg));
    String s_block = String.valueOf(block);
    if (rec instanceof Player) {
      Player p = (Player)rec;
      out((Entity)p, type.getMsgReceiver().replace("%dmg%", s_dmg)
          .replace("%entity%", d_name)
          .replace("%amount%", s_block));
      type.playSound(p);
    } 
    if (dd instanceof Projectile) {
      Projectile pp = (Projectile)dd;
      if (pp.getShooter() != null && pp.getShooter() instanceof Player)
        dd = (Entity)pp.getShooter(); 
    } 
    if (dd instanceof Player) {
      Player p = (Player)dd;
      out((Entity)p, type.getMsgDamager().replace("%dmg%", s_dmg)
          .replace("%entity%", rec_name)
          .replace("%amount%", s_block));
      type.playSound(p);
    } 
  }
  
  public String getDamageTypeFormat(String type) {
    type = type.toLowerCase();
    if (this.ind_f_dt.containsKey(type))
      return this.ind_f_dt.get(type); 
    return "§cError. Invalid Type!";
  }
  
  public String getDamageTypeSource(String type) {
    type = type.toUpperCase();
    if (this.ind_f_ds.containsKey(type))
      return this.ind_f_ds.get(type); 
    if (this.ind_f_ds.containsKey("DEFAULT"))
      return this.ind_f_ds.get("DEFAULT"); 
    return "§cError. Invalid Type!";
  }
  
  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onDamageIndicator(EntityDamageEvent ex) {
    if (ex.getEntity() instanceof org.bukkit.entity.ArmorStand)
      return; 
    DamageMeta e = MetaUtils.getDamageMeta(ex.getEntity());
    if (e == null)
      return; 
    MetaUtils.removeDamageMeta(ex.getEntity());
    Entity rec = e.getEntity();
    if (rec.isInvulnerable())
      return; 
    e.fixDefaultDamage();
    e.addMissingDmg(ex.getDamage());
    double dmg = e.getTotalDamage();
    if (dmg <= 0.0D && this.ignore_zero)
      return; 
    double blocked = e.getDamageBlocked();
    double pvpe_dmg = e.getPvPEDamage();
    double pvpe_def = e.getPvPEDefense();
    if (e.getDamager() != null) {
      Entity dd = e.getDamager();
      String rec_name = e.getEntityName();
      String d_name = Utils.getEntityName(dd);
      MessageType type = MessageType.NORMAL;
      if (e.isCritical()) {
        type = MessageType.CRITICAL;
      } else if (e.isDodged()) {
        type = MessageType.DODGE;
      } else if (e.isBlocked()) {
        type = MessageType.BLOCK;
      } 
      sendCombatMsg(type, dmg, blocked, rec_name, d_name, rec, dd);
    } 
    if (!this.ind_enabled)
      return; 
    if (!(rec instanceof LivingEntity))
      return; 
    List<String> list = new ArrayList<>();
    for (String s : this.ind_f_order) {
      if (s.equalsIgnoreCase("%dodge%") && e.isDodged()) {
        list.add(MessageType.DODGE.getMsgIndicator());
        break;
      } 
      if (s.equalsIgnoreCase("%crit%") && e.isCritical()) {
        list.add(MessageType.CRITICAL.getMsgIndicator());
        continue;
      } 
      if (s.equalsIgnoreCase("%block%") && e.isBlocked()) {
        list.add(MessageType.BLOCK.getMsgIndicator());
        continue;
      } 
      if (s.equalsIgnoreCase("%dmg%")) {
        for (String s2 : this.ind_f_dt.keySet()) {
          double d = e.getDamage(s2);
          d *= 1.0D + (pvpe_dmg - pvpe_def) / 100.0D;
          if (blocked > 0.0D)
            d *= 1.0D - blocked / 100.0D; 
          if (d > 0.0D)
            list.add(getDamageTypeFormat(s2).replace("%dmg%", String.valueOf(Utils.round3(d)))); 
        } 
        for (String s2 : this.ind_f_ds.keySet()) {
          double d = e.getDamageCause(s2);
          d *= 1.0D + (pvpe_dmg - pvpe_def) / 100.0D;
          if (blocked > 0.0D)
            d *= 1.0D - blocked / 100.0D; 
          if (d > 0.0D)
            list.add(getDamageTypeSource(s2).replace("%dmg%", String.valueOf(Utils.round3(d)))); 
        } 
      } 
    } 
    if (!list.isEmpty()) {
      LivingEntity li = (LivingEntity)rec;
      Location loc = li.getEyeLocation().clone().add(0.0D, 1.25D, 0.0D);
      HDHook hd = (HDHook)EHook.HOLOGRAPHIC_DISPLAYS.getHook();
      hd.createIndicator(loc, list);
    } 
  }
  
  public enum MessageType {
    NORMAL, CRITICAL, DODGE, BLOCK;
    
    private boolean enabled;
    
    private String msg_d;
    
    private String msg_r;
    
    private String msg_i;
    
    private Sound s;
    
    MessageType() {
      this.enabled = true;
      this.msg_d = "null";
      this.msg_r = "null";
      this.msg_i = "";
      this.s = null;
    }
    
    public boolean isEnabled() {
      return this.enabled;
    }
    
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
    
    public String getMsgDamager() {
      return this.msg_d;
    }
    
    public void setMsgDamager(String msg_d) {
      this.msg_d = msg_d;
    }
    
    public String getMsgReceiver() {
      return this.msg_r;
    }
    
    public void setMsgReceiver(String msg_r) {
      this.msg_r = msg_r;
    }
    
    public String getMsgIndicator() {
      return this.msg_i;
    }
    
    public void setMsgIndicator(String msg_i) {
      this.msg_i = msg_i;
    }
    
    public void setSound(Sound s) {
      this.s = s;
    }
    
    public void playSound(Player p) {
      if (this.s != null)
        p.playSound(p.getLocation(), this.s, 0.8F, 0.8F); 
    }
  }
  
  public boolean isIgnoreZeroDamage() {
    return this.ignore_zero;
  }
}
