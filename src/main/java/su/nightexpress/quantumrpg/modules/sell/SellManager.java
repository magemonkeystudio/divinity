package su.nightexpress.quantumrpg.modules.sell;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.ItemAPI;
import su.nightexpress.quantumrpg.config.Config;
import su.nightexpress.quantumrpg.config.JYML;
import su.nightexpress.quantumrpg.gui.GUIItem;
import su.nightexpress.quantumrpg.gui.GUIUtils;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.modules.QModule;
import su.nightexpress.quantumrpg.modules.QModuleDrop;
import su.nightexpress.quantumrpg.modules.QModuleLevel;
import su.nightexpress.quantumrpg.modules.cmds.MExecutor;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.types.ArmorType;
import su.nightexpress.quantumrpg.types.DamageType;
import su.nightexpress.quantumrpg.types.QSlotType;
import su.nightexpress.quantumrpg.utils.Utils;
import su.nightexpress.quantumrpg.utils.logs.LogType;

public class SellManager extends QModule {
  private double lvl_def;
  
  private double lvl_for;
  
  private Map<EModule, Map<String, Double>> mod;
  
  private Map<String, Double> mat_cost;
  
  private Map<QSlotType, Double> sock;
  
  private Map<String, Double> ench;
  
  private SellGUI gui;
  
  public SellManager(QuantumRPG plugin, boolean enabled, MExecutor exec) {
    super(plugin, enabled, exec);
  }
  
  public EModule type() {
    return EModule.SELL;
  }
  
  public String name() {
    return "Sell";
  }
  
  public String version() {
    return "1.0.2";
  }
  
  public boolean isResolvable() {
    return false;
  }
  
  public void setup() {
    if (!EHook.VAULT.isEnabled()) {
      log("Can't run without Vault!", LogType.ERROR);
      unload();
      return;
    } 
    this.mod = new HashMap<>();
    this.mat_cost = new HashMap<>();
    this.sock = new HashMap<>();
    this.ench = new HashMap<>();
    setupCfg();
  }
  
  public void updateCfg() {
    JYML jYML = this.cfg.getConfig();
    byte b;
    int i;
    EModule[] arrayOfEModule;
    for (i = (arrayOfEModule = EModule.values()).length, b = 0; b < i; ) {
      EModule e = arrayOfEModule[b];
      if (e.isEnabled()) {
        QModule q = this.plugin.getMM().getModule(e);
        if (q instanceof QModuleDrop) {
          QModuleDrop q2 = (QModuleDrop)q;
          double d = 50.0D;
          if (jYML.contains("price.module-types." + e.name()))
            d = jYML.getDouble("price.module-types." + e.name(), 50.0D); 
          if (!jYML.contains("price.item-modules." + e.name())) {
            jYML.set("price.item-modules." + e.name() + ".default", Double.valueOf(d));
            for (String id : q2.getItemIds()) {
              if (id.equalsIgnoreCase("random"))
                continue; 
              jYML.set("price.item-modules." + e.name() + "." + id, Double.valueOf(50.0D));
            } 
          } 
        } 
      } 
      b++;
    } 
    jYML.set("price.module-types", null);
    if (!jYML.isConfigurationSection("price.enchants")) {
      Enchantment[] arrayOfEnchantment;
      for (i = (arrayOfEnchantment = Enchantment.values()).length, b = 0; b < i; ) {
        Enchantment e = arrayOfEnchantment[b];
        jYML.set("price.enchants." + e.getName(), Double.valueOf(50.0D));
        b++;
      } 
    } 
    ItemStat[] arrayOfItemStat;
    for (i = (arrayOfItemStat = ItemStat.values()).length, b = 0; b < i; ) {
      ItemStat e = arrayOfItemStat[b];
      if (e != ItemStat.SALE_PRICE && 
        !jYML.contains("price.item-stats." + e.name()))
        jYML.set("price.item-stats." + e.name(), Double.valueOf(50.0D)); 
      b++;
    } 
    for (DamageType dt : Config.getDamageTypes().values()) {
      if (!jYML.contains("price.damage-types." + dt.getId()))
        jYML.set("price.damage-types." + dt.getId(), Double.valueOf(25.0D)); 
    } 
    for (ArmorType dt : Config.getArmorTypes().values()) {
      if (!jYML.contains("price.armor-types." + dt.getId()))
        jYML.set("price.armor-types." + dt.getId(), Double.valueOf(25.0D)); 
    } 
    this.cfg.save();
  }
  
  public void shutdown() {
    this.mat_cost = null;
    if (this.gui != null) {
      this.gui.shutdown();
      this.gui = null;
    } 
  }
  
  private void setupCfg() {
    JYML jYML = this.cfg.getConfig();
    String path = "price.";
    this.lvl_def = jYML.getDouble(String.valueOf(path) + "levels.default");
    this.lvl_for = jYML.getDouble(String.valueOf(path) + "levels.for-each");
    if (jYML.isConfigurationSection(String.valueOf(path) + "item-types"))
      for (String m : jYML.getConfigurationSection(String.valueOf(path) + "item-types").getKeys(false)) {
        Material mat = Material.getMaterial(m.toUpperCase());
        if (mat == null) {
          log("Invalid material '" + m + "' in 'price.item-types'!", LogType.WARN);
          continue;
        } 
        double c = jYML.getDouble(String.valueOf(path) + "item-types." + m);
        this.mat_cost.put(m.toUpperCase(), Double.valueOf(c));
      }  
    if (jYML.isConfigurationSection(String.valueOf(path) + "armor-types"))
      for (String m : jYML.getConfigurationSection(String.valueOf(path) + "armor-types").getKeys(false)) {
        ArmorType at = Config.getArmorTypeById(m);
        if (at == null) {
          log("Invalid armor type '" + m + "' in 'price.armor-types'!", LogType.WARN);
          continue;
        } 
        double c = jYML.getDouble(String.valueOf(path) + "armor-types." + m);
        at.setCost(c);
      }  
    if (jYML.isConfigurationSection(String.valueOf(path) + "damage-types"))
      for (String m : jYML.getConfigurationSection(String.valueOf(path) + "damage-types").getKeys(false)) {
        DamageType at = Config.getDamageTypeById(m);
        if (at == null) {
          log("Invalid damage type '" + m + "' in 'price.damage-types'!", LogType.WARN);
          continue;
        } 
        double c = jYML.getDouble(String.valueOf(path) + "damage-types." + m);
        at.setCost(c);
      }  
    if (jYML.isConfigurationSection(String.valueOf(path) + "enchants"))
      for (String m : jYML.getConfigurationSection(String.valueOf(path) + "enchants").getKeys(false)) {
        Enchantment e = Enchantment.getByName(m.toUpperCase());
        if (e == null) {
          log("Invalid Enchantment '" + m + "' in 'price.enchants'!", LogType.WARN);
          continue;
        } 
        double c = jYML.getDouble(String.valueOf(path) + "damage-types." + m);
        this.ench.put(m.toUpperCase(), Double.valueOf(c));
      }  
    byte b;
    int j;
    EModule[] arrayOfEModule;
    for (j = (arrayOfEModule = EModule.values()).length, b = 0; b < j; ) {
      EModule e = arrayOfEModule[b];
      Map<String, Double> mc = new HashMap<>();
      String path2 = String.valueOf(path) + "item-modules." + e.name();
      if (jYML.isConfigurationSection(path2))
        for (String id : jYML.getConfigurationSection(path2).getKeys(false)) {
          double c = jYML.getDouble(String.valueOf(path2) + "." + id);
          mc.put(id.toLowerCase(), Double.valueOf(c));
        }  
      this.mod.put(e, mc);
      b++;
    } 
    ItemStat[] arrayOfItemStat;
    for (j = (arrayOfItemStat = ItemStat.values()).length, b = 0; b < j; ) {
      ItemStat e = arrayOfItemStat[b];
      if (e != ItemStat.SALE_PRICE) {
        double c = jYML.getDouble(String.valueOf(path) + "item-stats." + e.name());
        e.setCost(c);
      } 
      b++;
    } 
    path = "gui.";
    String g_title = ChatColor.translateAlternateColorCodes('&', jYML.getString(String.valueOf(path) + "title"));
    int g_size = jYML.getInt(String.valueOf(path) + "size");
    String[] raw = jYML.getString(String.valueOf(path) + "item-slots").replaceAll("\\s", "").split(",");
    int[] slots = new int[raw.length];
    for (int i = 0; i < raw.length; i++) {
      try {
        slots[i] = Integer.parseInt(raw[i].trim());
      } catch (NumberFormatException numberFormatException) {}
    } 
    LinkedHashMap<String, GUIItem> items = new LinkedHashMap<>();
    if (jYML.isConfigurationSection(String.valueOf(path) + "content"))
      for (String id : jYML.getConfigurationSection(String.valueOf(path) + "content").getKeys(false)) {
        GUIItem gi = GUIUtils.getItemFromSection((FileConfiguration)jYML, id, String.valueOf(path) + "content." + id + ".");
        items.put(id, gi);
      }  
    this.gui = new SellGUI(this, g_title, g_size, items, slots);
  }
  
  public void openSellGUI(Player p) {
    this.gui.open(p);
  }
  
  private double getModuleItemCost(ItemStack item) {
    double d = 0.0D;
    EModule e = ItemAPI.getItemModule(item);
    if (e != null) {
      QModuleDrop q = (QModuleDrop)this.plugin.getMM().getModule(e);
      if (this.mod.containsKey(e)) {
        String id = q.getItemId(item);
        Map<String, Double> map = this.mod.get(e);
        if (map.containsKey(id)) {
          d = ((Double)map.get(id)).doubleValue();
        } else if (map.containsKey("default")) {
          d = ((Double)map.get("default")).doubleValue();
        } 
      } 
      if (q instanceof QModuleLevel) {
        QModuleLevel ql = (QModuleLevel)q;
        int lvl = ql.getLevel(item);
        if (lvl > 0)
          d += this.lvl_def + this.lvl_for * (lvl - 1); 
      } 
    } 
    return d;
  }
  
  private double getEnchantItemCost(ItemStack item) {
    double d = 0.0D;
    ItemMeta meta = item.getItemMeta();
    if (meta == null || !meta.hasEnchants())
      return d; 
    for (Map.Entry<Enchantment, Integer> e : (Iterable<Map.Entry<Enchantment, Integer>>)meta.getEnchants().entrySet()) {
      String n = ((Enchantment)e.getKey()).getName();
      if (!this.ench.containsKey(n))
        continue; 
      int lvl = ((Integer)e.getValue()).intValue();
      d += ((Double)this.ench.get(n)).doubleValue();
      if (lvl > 0)
        d += this.lvl_def + this.lvl_for * (lvl - 1); 
    } 
    return d;
  }
  
  public double calcCost(ItemStack item) {
    double d = 0.0D;
    if (item == null || item.getType() == Material.AIR)
      return d; 
    d += getModuleItemCost(item);
    d += getEnchantItemCost(item);
    byte b;
    int i;
    ItemStat[] arrayOfItemStat;
    for (i = (arrayOfItemStat = ItemStat.values()).length, b = 0; b < i; ) {
      ItemStat is = arrayOfItemStat[b];
      if (is != ItemStat.SALE_PRICE && 
        ItemAPI.hasAttribute(item, is))
        d += is.getCost() * ItemAPI.getStatOnItem(item, is); 
      b++;
    } 
    for (DamageType dt : Config.getDamageTypes().values())
      d += dt.getCost() * ItemAPI.getDamageByTypeMinOrMax(dt.getId(), item, 1); 
    for (ArmorType dt : Config.getArmorTypes().values())
      d += dt.getCost() * ItemAPI.getDefenseByType(dt.getId(), item); 
    if (this.mat_cost.containsKey(item.getType().name()))
      d += ((Double)this.mat_cost.get(item.getType().name())).doubleValue(); 
    double sale = ItemAPI.getStatOnItem(item, ItemStat.SALE_PRICE);
    if (sale > 0.0D)
      d *= 1.0D + sale / 100.0D; 
    d *= item.getAmount();
    return Math.max(0.0D, Utils.round3(d));
  }
}
