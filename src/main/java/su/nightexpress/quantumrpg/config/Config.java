package su.nightexpress.quantumrpg.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.hooks.EHook;
import su.nightexpress.quantumrpg.hooks.HookClass;
import su.nightexpress.quantumrpg.hooks.HookLevel;
import su.nightexpress.quantumrpg.modules.EModule;
import su.nightexpress.quantumrpg.stats.BleedRateSettings;
import su.nightexpress.quantumrpg.stats.DisarmRateSettings;
import su.nightexpress.quantumrpg.stats.ItemStat;
import su.nightexpress.quantumrpg.stats.StatSettings;
import su.nightexpress.quantumrpg.types.AmmoType;
import su.nightexpress.quantumrpg.types.ArmorType;
import su.nightexpress.quantumrpg.types.DamageType;
import su.nightexpress.quantumrpg.types.ItemGroup;
import su.nightexpress.quantumrpg.types.ItemSubType;
import su.nightexpress.quantumrpg.types.WpnHand;
import su.nightexpress.quantumrpg.utils.logs.LogType;
import su.nightexpress.quantumrpg.utils.logs.LogUtil;

public class Config {
  private static QuantumRPG plugin = QuantumRPG.instance;
  
  private static MyConfig config;
  
  private static FileConfiguration cfg;
  
  public static String lang;
  
  public static HookLevel g_LevelPlugin;
  
  public static HookClass g_ClassPlugin;
  
  public static String global_dmg_formula;
  
  public static double g_dmgReduce;
  
  public static boolean g_itemsBreak;
  
  public static int g_targetDist;
  
  public static boolean g_mobAtt;
  
  public static boolean g_fishHookDmg;
  
  public static boolean g_offAtt;
  
  public static boolean g_sapiDur;
  
  public static boolean g_mobsDur;
  
  public static boolean g_bowsMel;
  
  public static boolean g_offSweep;
  
  public static boolean g_holdRestrict;
  
  public static boolean g_useDropsV2;
  
  public static double g_combat_shield_rate;
  
  public static double g_combat_shield_dmg;
  
  public static int g_combat_shield_cd;
  
  public static Map<EntityDamageEvent.DamageCause, Double> damage_values_p;
  
  public static Map<EntityDamageEvent.DamageCause, Double> damage_values_m;
  
  public static Map<String, DamageType> damage_types;
  
  public static Map<String, ArmorType> armor_types;
  
  public static String str_dmgSep;
  
  public static String str_durSep;
  
  public static String str_durUnb;
  
  public static String str_procent;
  
  public static String str_negative;
  
  public static String str_positive;
  
  public static String str_Modifier;
  
  public static String str_Separ_Char;
  
  public static String str_Separ_Color;
  
  public static String str_Req_Lvl_Item_Range;
  
  public static String str_Req_Lvl_Item_Single;
  
  public static String str_Req_Lvl_User_Single;
  
  public static String str_Req_Cls_User_Single;
  
  public static String str_Req_Cls_Item_Single;
  
  public static Map<String, ItemSubType> item_subs;
  
  public static void setup() {
    config = (plugin.getCM()).configMain;
    cfg = (FileConfiguration)config.getConfig();
    load();
  }
  
  private static void update() {
    addMissing("general.allow-bows-melee-damage", Boolean.valueOf(false));
    addMissing("general.allow-hold-items-you-cant-use", Boolean.valueOf(false));
    addMissing("general.disable-vanilla-sweep-attack", Boolean.valueOf(false));
    addMissing("general.lore-format.hand-type", "&7Hand: %type_name%");
    addMissing("general.use-drops-module-v2", Boolean.valueOf(false));
    addMissing("general.combat.shield-block-add-rate", Double.valueOf(50.0D));
    addMissing("general.combat.shield-block-damage", Double.valueOf(50.0D));
    addMissing("general.combat.shield-block-cooldown", Integer.valueOf(5));
    addMissing("strings.separator.symbol", "&7/");
    addMissing("strings.separator.value-color", "&f");
    addMissing("strings.durability-unbreakable", "Unbreakable");
    addMissing("strings.requirements.level.item.range", "&c➛ Item Lvl required: %min%-%max%");
    addMissing("strings.requirements.level.item.single", "&c➛ Item Lvl required: %min%+");
    addMissing("strings.requirements.level.player", "%state%Player Lvl: %lvl%+");
    addMissing("strings.requirements.class.player", "%state%Player Class: %class%");
    addMissing("strings.requirements.class.item", "&c➛ Item type: %type%");
    byte b;
    int i;
    WpnHand[] arrayOfWpnHand;
    for (i = (arrayOfWpnHand = WpnHand.values()).length, b = 0; b < i; ) {
      WpnHand wh = arrayOfWpnHand[b];
      String path = "hand-types." + wh.name() + ".";
      addMissing(String.valueOf(path) + "enabled", Boolean.valueOf(true));
      addMissing(String.valueOf(path) + "name", wh.getName());
      b++;
    } 
    ItemStat[] arrayOfItemStat;
    for (i = (arrayOfItemStat = ItemStat.values()).length, b = 0; b < i; ) {
      ItemStat is = arrayOfItemStat[b];
      String path = "item-stats." + is.name() + ".";
      addMissing(String.valueOf(path) + "name", is.getName());
      addMissing(String.valueOf(path) + "prefix", is.getPrefix());
      addMissing(String.valueOf(path) + "value", is.getValue());
      addMissing(String.valueOf(path) + "capability", Double.valueOf(is.getCapability()));
      cfg.set(String.valueOf(path) + "bonus", null);
      b++;
    } 
    EModule[] arrayOfEModule;
    for (i = (arrayOfEModule = EModule.values()).length, b = 0; b < i; ) {
      EModule e = arrayOfEModule[b];
      addMissing("modules." + e.name(), Boolean.valueOf(true));
      b++;
    } 
  }
  
  private static void addMissing(String path, Object o) {
    if (cfg.contains(path))
      return; 
    cfg.set(path, o);
  }
  
  public static void save() {
    config.save();
  }
  
  private static void load() {
    update();
    save();
    lang = cfg.getString("lang");
    String path = "general.";
    String p1 = cfg.getString(String.valueOf(path) + "level-plugin");
    String p2 = cfg.getString(String.valueOf(path) + "class-plugin");
    byte b1;
    int i;
    EHook[] arrayOfEHook;
    for (i = (arrayOfEHook = EHook.values()).length, b1 = 0; b1 < i; ) {
      EHook h = arrayOfEHook[b1];
      if (h.isLevel() && h.getPluginName().equalsIgnoreCase(p1))
        g_LevelPlugin = (HookLevel)h.getHook(); 
      if (h.isClass() && h.getPluginName().equalsIgnoreCase(p2))
        g_ClassPlugin = (HookClass)h.getHook(); 
      b1++;
    } 
    if (g_LevelPlugin == null)
      g_LevelPlugin = (HookLevel)EHook.NONE.getHook(); 
    if (g_ClassPlugin == null)
      g_ClassPlugin = (HookClass)EHook.NONE.getHook(); 
    global_dmg_formula = cfg.getString(String.valueOf(path) + "damage-formula");
    g_dmgReduce = cfg.getDouble(String.valueOf(path) + "damage-reduce-by-cooldown");
    g_itemsBreak = cfg.getBoolean(String.valueOf(path) + "break-items");
    g_targetDist = cfg.getInt(String.valueOf(path) + "max-get-target-distance");
    g_mobAtt = cfg.getBoolean(String.valueOf(path) + "item-stats-applies-to-mobs");
    g_fishHookDmg = cfg.getBoolean(String.valueOf(path) + "fish-hook-can-damage");
    g_offAtt = cfg.getBoolean(String.valueOf(path) + "item-stats-applies-in-offhand");
    g_sapiDur = cfg.getBoolean(String.valueOf(path) + "skill-api-skills-can-reduce-item-durability");
    g_mobsDur = cfg.getBoolean(String.valueOf(path) + "mobs-can-reduce-item-durability");
    g_bowsMel = cfg.getBoolean(String.valueOf(path) + "allow-bows-melee-damage");
    g_offSweep = cfg.getBoolean(String.valueOf(path) + "disable-vanilla-sweep-attack");
    g_holdRestrict = cfg.getBoolean(String.valueOf(path) + "allow-hold-items-you-cant-use");
    g_useDropsV2 = cfg.getBoolean(String.valueOf(path) + "use-drops-module-v2");
    g_combat_shield_rate = cfg.getDouble(String.valueOf(path) + "combat.shield-block-add-rate");
    g_combat_shield_dmg = cfg.getDouble(String.valueOf(path) + "combat.shield-block-damage");
    g_combat_shield_cd = cfg.getInt(String.valueOf(path) + "combat.shield-block-cooldown");
    path = "general.lore-format.";
    String g_attFormat = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "item-stat"));
    String g_dtFormat = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "damage-type"));
    String g_atFormat = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "armor-type"));
    String g_ammoFormat = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "ammo-type"));
    for (String n : cfg.getConfigurationSection("modules").getKeys(false)) {
      EModule e1;
      try {
        e1 = EModule.valueOf(n.toUpperCase());
      } catch (IllegalArgumentException ex) {
        LogUtil.send("Unknown module: '" + n + "'!", LogType.WARN);
        continue;
      } 
      boolean b = cfg.getBoolean("modules." + n);
      e1.setEnabled(b);
    } 
    damage_values_p = new HashMap<>();
    damage_values_m = new HashMap<>();
    for (String s : cfg.getConfigurationSection("damage-modifiers").getKeys(false)) {
      try {
        EntityDamageEvent.DamageCause dt = EntityDamageEvent.DamageCause.valueOf(s.toUpperCase());
        double d1 = cfg.getDouble("damage-modifiers." + s + ".PLAYER");
        double d2 = cfg.getDouble("damage-modifiers." + s + ".MOB");
        damage_values_p.put(dt, Double.valueOf(d1));
        damage_values_m.put(dt, Double.valueOf(d2));
      } catch (IllegalArgumentException e1) {}
    } 
    damage_types = new HashMap<>();
    for (String id : cfg.getConfigurationSection("damage-types").getKeys(false)) {
      path = "damage-types." + id + ".";
      boolean def = cfg.getBoolean(String.valueOf(path) + "default");
      String prefix = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "prefix"));
      String name = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "name"));
      String value = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "value"));
      List<String> actions = cfg.getStringList(String.valueOf(path) + "on-hit-actions");
      HashMap<String, Double> biome = new HashMap<>();
      if (cfg.contains(String.valueOf(path) + "biome-damage-modifier"))
        for (String b : cfg.getConfigurationSection(String.valueOf(path) + "biome-damage-modifier").getKeys(false)) {
          double bd = cfg.getDouble(String.valueOf(path) + "biome-damage-modifier." + b);
          biome.put(b.toUpperCase(), Double.valueOf(bd));
        }  
      DamageType dt = new DamageType(id, def, prefix, name, value, actions, biome, g_dtFormat);
      damage_types.put(dt.getId(), dt);
    } 
    armor_types = new HashMap<>();
    for (String id : cfg.getConfigurationSection("armor-types").getKeys(false)) {
      path = "armor-types." + id + ".";
      String prefix = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "prefix"));
      String name = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "name"));
      String value = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "value"));
      boolean percent = cfg.getBoolean(String.valueOf(path) + "percent");
      List<String> bds = new ArrayList<>();
      for (String s : cfg.getStringList(String.valueOf(path) + "block-damage-source"))
        bds.add(s.toLowerCase()); 
      List<String> bdt = new ArrayList<>();
      for (String s : cfg.getStringList(String.valueOf(path) + "block-damage-types"))
        bdt.add(s.toLowerCase()); 
      String formula = cfg.getString(String.valueOf(path) + "formula");
      ArmorType dt = new ArmorType(id, prefix, name, value, percent, bds, bdt, formula, g_atFormat);
      armor_types.put(dt.getId(), dt);
    } 
    for (String s : cfg.getConfigurationSection("item-stats").getKeys(false)) {
      ItemStat at = null;
      try {
        at = ItemStat.valueOf(s.toUpperCase());
      } catch (IllegalArgumentException ex) {
        continue;
      } 
      path = "item-stats." + at.name() + ".";
      String name = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "name"));
      String prefix = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "prefix"));
      String value = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "value"));
      double cap = cfg.getDouble(String.valueOf(path) + "capability");
      at.setName(name);
      at.setPrefix(prefix);
      at.setValue(value);
      at.setCapability(cap);
      at.setFormat(g_attFormat);
      if (at == ItemStat.DISARM_RATE) {
        path = "item-stats-settings." + at.name() + ".";
        String eff = cfg.getString(String.valueOf(path) + "effect");
        String msg_d = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "message.damager"));
        String msg_e = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "message.entity"));
        DisarmRateSettings drs = new DisarmRateSettings(at, eff, msg_d, msg_e);
        at.setSettings((StatSettings)drs);
        continue;
      } 
      if (at == ItemStat.BLEED_RATE) {
        path = "item-stats-settings." + at.name() + ".";
        int time = cfg.getInt(String.valueOf(path) + "time");
        String formula = cfg.getString(String.valueOf(path) + "formula");
        String eff = cfg.getString(String.valueOf(path) + "effect");
        BleedRateSettings brs = new BleedRateSettings(at, time, formula, eff);
        at.setSettings((StatSettings)brs);
      } 
    } 
    for (String s : cfg.getConfigurationSection("hand-types").getKeys(false)) {
      WpnHand at = null;
      try {
        at = WpnHand.valueOf(s.toUpperCase());
      } catch (IllegalArgumentException ex) {
        continue;
      } 
      path = "hand-types." + at.name() + ".";
      boolean ena = cfg.getBoolean(String.valueOf(path) + "enabled");
      String name = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "name"));
      at.setEnabled(ena);
      at.setName(name);
      at.setFormat(cfg.getString("general.lore-format.hand-type"));
    } 
    for (String s : cfg.getConfigurationSection("ammo-types").getKeys(false)) {
      AmmoType at = null;
      try {
        at = AmmoType.valueOf(s.toUpperCase());
      } catch (IllegalArgumentException ex) {
        continue;
      } 
      path = "ammo-types." + at.name() + ".";
      boolean ena = cfg.getBoolean(String.valueOf(path) + "enabled");
      String name = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "name"));
      String prefix = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "prefix"));
      at.setEnabled(ena);
      at.setName(name);
      at.setPrefix(prefix);
      at.setFormat(g_ammoFormat);
    } 
    path = "strings.";
    str_dmgSep = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "dmg-separator"));
    str_durSep = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "durability-separator"));
    str_durUnb = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "durability-unbreakable"));
    str_procent = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "percent"));
    str_negative = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "negative"));
    str_positive = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "positive"));
    str_Modifier = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "modifier"));
    str_Separ_Char = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "separator.symbol"));
    str_Separ_Color = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "separator.value-color"));
    str_Req_Lvl_Item_Range = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "requirements.level.item.range"));
    str_Req_Lvl_Item_Single = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "requirements.level.item.single"));
    str_Req_Lvl_User_Single = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "requirements.level.player"));
    str_Req_Cls_User_Single = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "requirements.class.player"));
    str_Req_Cls_Item_Single = ChatColor.translateAlternateColorCodes('&', cfg.getString(String.valueOf(path) + "requirements.class.item"));
    byte b2;
    int j;
    ItemGroup[] arrayOfItemGroup;
    for (j = (arrayOfItemGroup = ItemGroup.values()).length, b2 = 0; b2 < j; ) {
      ItemGroup ig = arrayOfItemGroup[b2];
      path = "item-groups." + ig.name() + ".";
      String name = cfg.getString(String.valueOf(path) + "name");
      ig.setName(name);
      List<String> list = cfg.getStringList(String.valueOf(path) + "materials");
      ig.setMaterials(list);
      b2++;
    } 
    item_subs = new HashMap<>();
    for (String sub : cfg.getConfigurationSection("item-sub-types").getKeys(false)) {
      path = "item-sub-types." + sub + ".";
      String name = cfg.getString(String.valueOf(path) + "name");
      List<String> mats = cfg.getStringList(String.valueOf(path) + "materials");
      ItemSubType ist = new ItemSubType(sub, name, mats);
      item_subs.put(ist.getId(), ist);
    } 
  }
  
  public static String getLangCode() {
    return lang;
  }
  
  public static HookLevel getLevelPlugin() {
    return g_LevelPlugin;
  }
  
  public static HookClass getClassPlugin() {
    return g_ClassPlugin;
  }
  
  public static String getDamageFormula() {
    return global_dmg_formula;
  }
  
  public static double getDamageCDReduce() {
    return g_dmgReduce;
  }
  
  public static boolean breakItems() {
    return g_itemsBreak;
  }
  
  public static int getMaxTargetDistance() {
    return g_targetDist;
  }
  
  public static boolean allowAttributesToMobs() {
    return g_mobAtt;
  }
  
  public static boolean allowAttributesToOffHand() {
    return g_offAtt;
  }
  
  public static boolean allowFishHookDamage() {
    return g_fishHookDmg;
  }
  
  public static boolean mobsCanReduceDurability() {
    return g_mobsDur;
  }
  
  public static boolean skillAPIReduceDurability() {
    return g_sapiDur;
  }
  
  public static boolean bowsMeleeDmg() {
    return g_bowsMel;
  }
  
  public static boolean noSweepAtk() {
    return g_offSweep;
  }
  
  public static Map<EntityDamageEvent.DamageCause, Double> getPlayerDmgModifiers() {
    return damage_values_p;
  }
  
  public static Map<EntityDamageEvent.DamageCause, Double> getMobDmgModifiers() {
    return damage_values_m;
  }
  
  public static Map<String, DamageType> getDamageTypes() {
    return damage_types;
  }
  
  public static Map<String, ArmorType> getArmorTypes() {
    return armor_types;
  }
  
  public static DamageType getDamageTypeById(String id) {
    return damage_types.get(id.toLowerCase());
  }
  
  public static DamageType getDamageTypeByDefault() {
    for (DamageType dt : getDamageTypes().values()) {
      if (dt.isDefault())
        return dt; 
    } 
    return null;
  }
  
  public static ArmorType getArmorTypeById(String id) {
    return armor_types.get(id.toLowerCase());
  }
  
  public static List<String> getSubTypeIds() {
    return new ArrayList<>(item_subs.keySet());
  }
  
  public static ItemSubType getSubTypeById(String id) {
    return item_subs.get(id.toLowerCase());
  }
  
  public static ItemSubType getItemSubType(ItemStack item) {
    return getItemSubType(item.getType());
  }
  
  public static ItemSubType getItemSubType(Material m) {
    return getItemSubType(m.name());
  }
  
  public static ItemSubType getItemSubType(String mat) {
    for (ItemSubType ist : item_subs.values()) {
      if (ist.isItemOfThis(mat))
        return ist; 
    } 
    return null;
  }
}
