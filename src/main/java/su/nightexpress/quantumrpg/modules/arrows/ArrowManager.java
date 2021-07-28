package su.nightexpress.quantumrpg.modules.arrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.DivineItemsAPI;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.config.MyConfig;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.ModuleItem;
import su.nightexpress.quantumrpg.modules.QModuleDrop;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.types.ArmorType;
import su.nightexpress.quantumrpg.types.DamageType;
import su.nightexpress.quantumrpg.utils.logs.LogType;

public class ArrowManager extends QModuleDrop {
  private MyConfig arrowsCfg;
  
  private List<Projectile> pjs;
  
  private int taskId;
  
  private final String NBT_KEY_ARROW = "QRPG_ARROW_ID";
  
  public ArrowManager(QuantumRPG plugin, boolean enabled, MExecutor exec) {
    super(plugin, enabled, exec);
  }
  
  public EModule type() {
    return EModule.ARROWS;
  }
  
  public String name() {
    return "Arrows";
  }
  
  public String version() {
    return "1.0";
  }
  
  public boolean isResolvable() {
    return true;
  }
  
  public void updateCfg() {}
  
  public void setup() {
    this.arrowsCfg = new MyConfig((JavaPlugin)this.plugin, "/modules/" + getId(), "arrows.yml");
    this.pjs = new ArrayList<>();
    setupCfg();
    startTask();
  }
  
  public void shutdown() {
    stopTask();
    this.pjs = null;
  }
  
  private void setupCfg() {
    JYML jYML = this.arrowsCfg.getConfig();
    if (!jYML.isConfigurationSection("arrows"))
      return; 
    for (String id : jYML.getConfigurationSection("arrows").getKeys(false)) {
      String path = "arrows." + id + ".";
      HashMap<ItemStat, Double> stats = new HashMap<>();
      if (jYML.isConfigurationSection(String.valueOf(path) + "additional-stats"))
        for (String a : jYML.getConfigurationSection(String.valueOf(path) + "additional-stats").getKeys(false)) {
          ItemStat at = null;
          try {
            at = ItemStat.valueOf(a.toUpperCase());
          } catch (IllegalArgumentException ex) {
            log("Invalid item stat '" + a + "' for arrow '" + id + "'!", LogType.WARN);
            continue;
          } 
          double val = jYML.getDouble(String.valueOf(path) + "additional-stats." + a);
          stats.put(at, Double.valueOf(val));
        }  
      HashMap<String, Double> dmg = new HashMap<>();
      if (jYML.isConfigurationSection(String.valueOf(path) + "additional-damage"))
        for (String a : jYML.getConfigurationSection(String.valueOf(path) + "additional-damage").getKeys(false)) {
          DamageType dt = Config.getDamageTypeById(a);
          if (dt == null) {
            log("Invalid damage type '" + a + "' for arrow '" + id + "'!", LogType.WARN);
            continue;
          } 
          double val = jYML.getDouble(String.valueOf(path) + "additional-damage." + a);
          dmg.put(a.toLowerCase(), Double.valueOf(val));
        }  
      HashMap<String, Double> def = new HashMap<>();
      if (jYML.isConfigurationSection(String.valueOf(path) + "defense-ignoring"))
        for (String a : jYML.getConfigurationSection(String.valueOf(path) + "defense-ignoring").getKeys(false)) {
          ArmorType dt = Config.getArmorTypeById(a);
          if (dt == null) {
            log("Invalid defense type '" + a + "' for arrow '" + id + "'!", LogType.WARN);
            continue;
          } 
          double val = jYML.getDouble(String.valueOf(path) + "defense-ignoring." + a);
          def.put(a.toLowerCase(), Double.valueOf(val));
        }  
      List<String> fly = jYML.getStringList(String.valueOf(path) + "on-fly-actions");
      List<String> hit = jYML.getStringList(String.valueOf(path) + "on-hit-actions");
      QArrow da = new QArrow(id, path, (FileConfiguration)jYML, type(), stats, dmg, def, fly, hit);
      this.items.put(da.getId(), da);
    } 
  }
  
  private void startTask() {
    this.taskId = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.plugin, new Runnable() {
          public void run() {
            ArrowManager.this.runFlyActions();
          }
        },  10L, 1L);
  }
  
  private void stopTask() {
    this.plugin.getServer().getScheduler().cancelTask(this.taskId);
  }
  
  public boolean isArrow(Projectile pj) {
    if (pj == null)
      return false; 
    return pj.hasMetadata("QRPG_ARROW_ID");
  }
  
  public String getArrowId(Projectile pj) {
    return ((MetadataValue)pj.getMetadata("QRPG_ARROW_ID").get(0)).asString();
  }
  
  public ItemStack getFirstArrow(Player p) {
    ItemStack off = p.getInventory().getItemInOffHand();
    if (isItemOfThisModule(off))
      return off; 
    int i = p.getInventory().first(Material.ARROW);
    if (i >= 0)
      return p.getInventory().getItem(i); 
    return null;
  }
  
  private void markArrow(String id, Projectile pj, LivingEntity launcher) {
    pj.setMetadata("QRPG_ARROW_ID", (MetadataValue)new FixedMetadataValue((Plugin)this.plugin, id));
  }
  
  public void runFlyActions() {
    List<Projectile> list = new ArrayList<>(this.pjs);
    for (Projectile pj : list) {
      if (pj.isOnGround() || !pj.isValid()) {
        this.pjs.remove(pj);
        continue;
      } 
      String id = getArrowId(pj);
      QArrow da = (QArrow)getItemById(id, QArrow.class);
      if (da == null || da.getFlyActions() == null)
        return; 
      DivineItemsAPI.executeActions((Entity)pj, da.getFlyActions(), null);
    } 
  }
  
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onProj(EntityShootBowEvent e) {
    if (!(e.getProjectile() instanceof Projectile))
      return; 
    Projectile pj = (Projectile)e.getProjectile();
    if (!(e.getEntity() instanceof Player))
      return; 
    Player p = (Player)e.getEntity();
    ItemStack arrow = getFirstArrow(p);
    if (!isItemOfThisModule(arrow))
      return; 
    String id = getItemId(arrow);
    markArrow(id, pj, (LivingEntity)p);
    this.pjs.add(pj);
  }
  
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onHit(ProjectileHitEvent e) {
    Projectile pj = e.getEntity();
    if (pj.getShooter() == null || !(pj.getShooter() instanceof Player))
      return; 
    if (!isArrow(pj))
      return; 
    String id = getArrowId(pj);
    Entity ee = e.getHitEntity();
    if (ee != null) {
      Location first = ee.getLocation();
      Location second = pj.getLocation();
      Vector from = first.toVector();
      Vector to = second.toVector();
      Vector direction = to.subtract(from);
      direction.normalize();
      direction.multiply(2);
      first.setDirection(direction);
      pj.teleport(first);
      pj.setVelocity(direction);
      pj.setMetadata("DI_TARGET", (MetadataValue)new FixedMetadataValue((Plugin)this.plugin, ee));
    } 
    QArrow da = (QArrow)getItemById(id, QArrow.class);
    DivineItemsAPI.executeActions((Entity)pj, da.getHitActions(), null);
  }
  
  public class QArrow extends ModuleItem {
    private HashMap<ItemStat, Double> stats;
    
    private HashMap<String, Double> dmg;
    
    private HashMap<String, Double> def;
    
    private List<String> fly;
    
    private List<String> hit;
    
    public QArrow(String id, String path, FileConfiguration cfg, EModule module, HashMap<ItemStat, Double> stats, HashMap<String, Double> dmg, HashMap<String, Double> def, List<String> fly, List<String> hit) {
      super(id, path, cfg, ArrowManager.this.type());
      setStats(stats);
      setDamage(dmg);
      setDefense(def);
      setFlyActions(fly);
      setHitActions(hit);
    }
    
    public HashMap<ItemStat, Double> getStats() {
      HashMap<ItemStat, Double> map = this.stats;
      byte b;
      int i;
      ItemStat[] arrayOfItemStat;
      for (i = (arrayOfItemStat = ItemStat.values()).length, b = 0; b < i; ) {
        ItemStat at = arrayOfItemStat[b];
        if (!map.containsKey(at))
          map.put(at, Double.valueOf(0.0D)); 
        b++;
      } 
      return map;
    }
    
    public double getAddStat(ItemStat stat) {
      if (this.stats.containsKey(stat))
        return ((Double)this.stats.get(stat)).doubleValue(); 
      return 0.0D;
    }
    
    public void setStats(HashMap<ItemStat, Double> stats) {
      this.stats = stats;
    }
    
    public HashMap<String, Double> getDamage() {
      return this.dmg;
    }
    
    public double getAddDamage(String d) {
      d = d.toLowerCase();
      if (this.dmg.containsKey(d))
        return ((Double)this.dmg.get(d)).doubleValue(); 
      return 0.0D;
    }
    
    public void setDamage(HashMap<String, Double> dmg) {
      this.dmg = dmg;
    }
    
    public HashMap<String, Double> getDefense() {
      return this.def;
    }
    
    public double getDefIgnore(String def) {
      def = def.toLowerCase();
      if (this.def.containsKey(def))
        return ((Double)this.def.get(def)).doubleValue(); 
      return 0.0D;
    }
    
    public void setDefense(HashMap<String, Double> def) {
      this.def = def;
    }
    
    public List<String> getFlyActions() {
      return this.fly;
    }
    
    public void setFlyActions(List<String> fly) {
      this.fly = fly;
    }
    
    public List<String> getHitActions() {
      return this.hit;
    }
    
    public void setHitActions(List<String> hit) {
      this.hit = hit;
    }
    
    protected ItemStack build() {
      ItemStack item = super.build();
      item.setType(Material.ARROW);
      return item;
    }
  }
}
